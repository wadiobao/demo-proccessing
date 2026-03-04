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
        // use embedding model to project text into high-dimensional semantic space
        // / sử dụng mô hình embedding để ánh xạ văn bản vào không gian ngữ nghĩa đa
        // chiều
        float[] vectorArray = embeddingModel.embed(text).content().vector();

        // convert float[] to List<Double> for strict alignment with MongoDB Atlas
        // Vector Search storage requirements
        // / chuyển float[] sang List<Double> để khớp hoàn toàn với yêu cầu lưu trữ của
        // MongoDB Atlas Vector Search
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
