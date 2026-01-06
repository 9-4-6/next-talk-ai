package com.gz.nexttalkai.config;

import com.gz.nexttalkai.advisor.MyLoggingAdvisor;
import com.gz.nexttalkai.tools.PowerTools;
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springaicommunity.tool.searcher.VectorToolSearcher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {


    @Bean
    ToolSearcher vectorToolSearcher(VectorStore vectorStore) {
        return new VectorToolSearcher(vectorStore);
    }

    /**
     * deepseek 客户端 如果存在多个大模型，就需要手动配置对应的客户端
     * @return
     */
    @Bean
    public ChatClient deepSeekChatClient(DeepSeekChatModel chatModel,ToolSearcher toolSearcher) {
        var toolSearchToolCallAdvisor = ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .referenceToolNameAccumulation(false)
                .maxResults(2)
                .build();
        return ChatClient.builder(chatModel)
                .defaultTools(new PowerTools())
                .defaultAdvisors(toolSearchToolCallAdvisor
                ).defaultAdvisors(new MyLoggingAdvisor())
                .build();

    }
}
