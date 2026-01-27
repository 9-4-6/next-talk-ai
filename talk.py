import os
import re
from dotenv import load_dotenv
from typing import TypedDict, List
from langgraph.graph import StateGraph, END
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_openai import ChatOpenAI

# 加载环境变量
load_dotenv()
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY")


# -------------------------- 1. 定义极简会话状态 --------------------------
class ConversationState(TypedDict):
    """仅保留核心字段，无多余状态"""
    user_input: str  # 用户当前输入
    intent: str  # 仅识别 check_electric_fee/other
    user_id: str  # 用电户号（单独提取，简化上下文）
    reply: str  # 客服回复
    step: int  # 会话步骤：0-初始,1-已要户号,2-已返回结果


# -------------------------- 2. 初始化 DeepSeek LLM --------------------------
def init_deepseek_llm():
    llm = ChatOpenAI(
        model_name="deepseek-chat",
        openai_api_base="https://api.deepseek.com/v1",
        openai_api_key=DEEPSEEK_API_KEY,
    )
    return llm


llm = init_deepseek_llm()


# -------------------------- 3. 极简意图识别（仅区分查电费/其他） --------------------------
def intent_recognition_node(state: ConversationState) -> ConversationState:
    """只识别是否是查电费意图，其他都归为other"""
    user_input = state["user_input"]
    prompt = ChatPromptTemplate.from_messages([
        ("system", "你只需要判断用户输入是否是查询电费，是返回check_electric_fee，否返回other，仅返回这两个值之一。"),
        ("human", "{user_input}")
    ])
    intent_chain = prompt | llm | StrOutputParser()
    intent = intent_chain.invoke({"user_input": user_input}).strip()

    # 强制简化意图（避免识别出错）
    state["intent"] = "check_electric_fee" if intent == "check_electric_fee" else "other"
    return state


# -------------------------- 4. 查电费核心节点（线性流程） --------------------------
def check_electric_fee_node(state: ConversationState) -> ConversationState:
    """
    按步骤执行：
    step0（初始）：用户说查电费 → 要户号，step=1
    step1（已要户号）：用户输户号 → 验证+返回电费，step=2
    """
    step = state["step"]
    user_input = state["user_input"]

    # 步骤0：用户刚说"查电费"，需要要户号
    if step == 0:
        state["reply"] = "为了帮你查询电费，请提供你的用电户号（如1234567890）。"
        state["step"] = 1  # 切换到步骤1（等待户号输入）

    # 步骤1：用户输入户号，验证并返回结果
    elif step == 1:
        # 提取10-12位纯数字户号
        id_match = re.search(r'\d{10,12}', user_input)
        if id_match:
            user_id = id_match.group()
            state["user_id"] = user_id
            state["reply"] = f"你的用电户号{user_id}本月电费为：125.6元，账单日期：2026-01-27，缴费截止日期：2026-02-15。"
            state["step"] = 2  # 切换到步骤2（完成查询）
        else:
            # 户号格式错误，直接提示（无循环，仅提示一次）
            state["reply"] = "你输入的户号格式错误（需10-12位纯数字），本次查询结束。如需重新查询，请再次输入'查电费'。"
            state["step"] = 0  # 重置为初始步骤

    # 步骤2：已返回结果，再次输入则提示
    elif step == 2:
        state["reply"] = "你已完成电费查询，如需再次查询，请重新输入'查电费'。"
        state["step"] = 0  # 重置步骤

    return state


# -------------------------- 5. 其他意图节点 --------------------------
def other_intent_node(state: ConversationState) -> ConversationState:
    """非查电费意图，直接提示"""
    state["reply"] = "暂仅支持电费查询服务，如需查询电费，请输入'查电费'。"
    state["step"] = 0  # 重置步骤
    return state


# -------------------------- 6. 路由函数（极简） --------------------------
def route_intent(state: ConversationState) -> str:
    """仅路由到查电费或其他节点"""
    return "check_electric_fee_node" if state["intent"] == "check_electric_fee" else "other_intent_node"


# -------------------------- 7. 构建无循环的线性图 --------------------------
def build_graph():
    graph = StateGraph(ConversationState)

    # 添加节点（无多余节点）
    graph.add_node("intent_recognition", intent_recognition_node)
    graph.add_node("check_electric_fee_node", check_electric_fee_node)
    graph.add_node("other_intent_node", other_intent_node)

    # 设置起始节点
    graph.set_entry_point("intent_recognition")

    # 线性路由：意图识别 → 对应节点 → 直接结束（无循环）
    graph.add_conditional_edges(
        "intent_recognition",
        route_intent,
        {
            "check_electric_fee_node": "check_electric_fee_node",
            "other_intent_node": "other_intent_node"
        }
    )

    # 所有节点执行完直接结束（核心：无循环，避免递归）
    graph.add_edge("check_electric_fee_node", END)
    graph.add_edge("other_intent_node", END)

    return graph.compile()


# -------------------------- 8. 测试极简线性会话 --------------------------
if __name__ == "__main__":
    # 初始化状态（极简，无多余字段）
    initial_state = ConversationState(
        user_input="",
        intent="",
        user_id="",
        reply="",
        step=0  # 初始步骤为0
    )

    app = build_graph()

    print("===== 电费客服极简会话 =====")
    print("输入 '退出' 结束会话")

    while True:
        user_input = input("\n你：")
        if user_input == "退出":
            print("客服：感谢你的咨询，再见！")
            break

        # 更新用户输入并运行图
        initial_state["user_input"] = user_input
        result = app.invoke(initial_state)

        # 输出回复，并更新初始状态（仅保留step和user_id，维持步骤）
        print(f"客服：{result['reply']}")
        initial_state["step"] = result["step"]
        initial_state["user_id"] = result["user_id"]