package com.gz.nexttalkai.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class PowerTools {
    @Tool(
            name = "query_electricity_bill",
            description = "查询用户指定月份的电费。如果未提供月份，则使用当前月份。参数 month: 字符串，格式 YYYY-MM，例如 2026-01"
    )
    public String queryElectricityBill(String month) {
        if (month == null || month.isBlank()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        // 实际替换为调用电力公司API
        return String.format("%s 月份电费：128.50 元，已缴清。", month);
    }

    @Tool(name = "query_power_outage", description = "查询停电信息。可选参数 area: 区域名称")
    public String queryPowerOutage(String area) {
        String location = area != null ? area : "您所在区域";
        return location + "近期无计划停电。";
    }

    @Tool(name = "apply_business_installation", description = "电力业务报装申请。参数 type: 报装类型，如 新装、增容")
    public String applyBusinessInstallation(String type) {
        return type + "报装申请已提交，预计3个工作日内联系您。";
    }

    @Tool(name = "file_complaint", description = "提交用电投诉。参数 content: 投诉内容")
    public String fileComplaint(String content) {
        return "您的投诉已记录（内容：" + content + "），工单号：C20260105001，请留意处理进度。";
    }

    @Tool(name = "transfer_to_human", description = "将对话转接给人工客服")
    public String transferToHuman() {
        return "正在为您转接人工客服，请稍等...";
    }
}
