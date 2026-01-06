package com.gz.nexttalkai.service;

import com.gz.nexttalkai.advisor.TokenCounterAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient deepSeekChatClient;

    public ChatService(@Qualifier("deepSeekChatClient") ChatClient deepSeekChatClient) {
        this.deepSeekChatClient = deepSeekChatClient;
    }

    public String chat(String userMessage) {
        return deepSeekChatClient
                .prompt()
                .user(userMessage)
                .system("""
                    你是电力客服AI助手“小电”。
                    当前日期是 2026年1月5日。
                    请根据用户意图，使用提供的工具查询信息。
                    如果用户想查电费但未指定月份，请主动询问“请问您要查询哪个月份的电费？（格式：2026-01）”。
                    如果用户仍未提供月份，则默认查询当前月份（2026-01）。
                    回复要友好、自然、简洁。
                    """)
                .advisors(new TokenCounterAdvisor())
                .call()
                .content();
    }

}
