package com.example.demo.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.embedding.EmbeddingModel;

@Component
public class VectorUtils {
	@Autowired
    private EmbeddingModel embeddingModel; // Inject từ cấu hình LangChain4j

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
