package com.gz.nexttalkai.config;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.tool.search.ToolReference;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class ToolVectorIndexConfig {
    private static final String GLOBAL_SESSION_ID = "global";  // 固定全局 ID

    @Bean
    public CommandLineRunner initializeGlobalTools(
            ToolSearcher toolSearcher,
            ApplicationContext context) {

        return args -> {
            log.info("=== 开始初始化全局工具到 chroma 向量数据库 ===");

            // 获取所有标记了 @Tool 的 Bean 中的方法
            Map<String, Object> toolBeans = context.getBeansWithAnnotation(org.springframework.ai.tool.annotation.Tool.class);

            for (Object bean : toolBeans.values()) {
                Class<?> clazz = bean.getClass();
                java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();

                for (java.lang.reflect.Method method : methods) {
                    if (method.isAnnotationPresent(org.springframework.ai.tool.annotation.Tool.class)) {
                        org.springframework.ai.tool.annotation.Tool toolAnn = method.getAnnotation(org.springframework.ai.tool.annotation.Tool.class);

                        String toolName = toolAnn.name().isEmpty() ?
                                method.getName() : toolAnn.name();

                        String description = toolAnn.description();

                        // 构建 ToolReference（社区项目提供的类）
                        ToolReference toolReference = new ToolReference(
                                toolName,
                                0.0,
                                description  // 传入实际 Method 对象，内部会解析参数 schema
                        );

                        // 使用固定全局 sessionId 索引
                        toolSearcher.indexTool(GLOBAL_SESSION_ID, toolReference);
                        log.info("已索引工具: " + toolName + " - " + description);
                    }
                }
            }
            log.info("=== 全局工具索引完成！可到 chroma 控制台查看 collection ===");
        };
    }
}
