package com.example.demo.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * Utility for semantic vector (embedding) operations.
 * 
 * <p>
 * Chuyển đổi văn bản thành các vectơ số học đa chiều (embeddings)
 * phục vụ cho việc tìm kiếm ngữ nghĩa và lưu trữ trong cơ sở dữ liệu Vector.
 *
 * @since 1.0
 */
@Component
public class VectorUtils {
    @Autowired
    private EmbeddingModel embeddingModel; // Inject từ cấu hình LangChain4j

    /**
     * Generates a high-dimensional vector representation of the input text.
     * 
     * @param text raw input string / văn bản thô cần chuyển đổi
     * @return list of doubles representing the embedding / danh sách các số thực
     *         biểu diễn vectơ
     */
    public List<Double> createVector(String text) {
        // 1. Dùng model để tạo embedding (trả về float[])
        float[] vectorArray = embeddingModel.embed(text).content().vector();

        // 2. Chuyển float[] sang List<Double> để tương thích với MongoDB Atlas
        return DoubleStream.of(convertFloatsToDoubles(vectorArray))
                .boxed()
                .collect(Collectors.toList());
    }

    private double[] convertFloatsToDoubles(float[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }
}
