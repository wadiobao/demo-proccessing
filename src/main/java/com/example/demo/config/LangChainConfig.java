package com.example.demo.configguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

@Configuration
public class LangChainConfig {

    @Bean
    EmbeddingModel embeddingModel() {
        /**
         * Mô hình AllMiniLmL6V2:
         * - Chạy trực tiếp trên CPU của bạn (Offline).
         * - Tạo ra vector 384 chiều (dimensions).
         * - Rất nhẹ và phù hợp cho việc so sánh nội dung văn bản.
         */
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
