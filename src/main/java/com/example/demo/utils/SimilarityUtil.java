package com.example.demo.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;

/**
 * Statistical text comparison utility.
 * 
 * <p>
 * Triển khai thuật toán Cosine Similarity để so sánh mức độ tương đồng
 * giữa hai chuỗi văn bản dựa trên vectơ tần suất từ (Term Frequency).
 *
 * @since 1.0
 */
public class SimilarityUtil {

    /**
     * Calculates the cosine similarity score between two strings.
     * 
     * @param text1 first text source / văn bản nguồn thứ nhất
     * @param text2 second text source / văn bản nguồn thứ hai
     * @return similarity index from 0.0 to 1.0 / chỉ số tương đồng từ 0.0 đến 1.0
     */
    public static double similarity(String text1, String text2) {
        Map<String, Integer> tf1 = termFrequency(text1);
        Map<String, Integer> tf2 = termFrequency(text2);

        Set<String> allWords = new HashSet<>();
        allWords.addAll(tf1.keySet());
        allWords.addAll(tf2.keySet());

        double dot = 0.0, mag1 = 0.0, mag2 = 0.0;

        for (String w : allWords) {
            int v1 = tf1.getOrDefault(w, 0);
            int v2 = tf2.getOrDefault(w, 0);

            dot += v1 * v2;
            mag1 += v1 * v1;
            mag2 += v2 * v2;
        }

        return (mag1 == 0 || mag2 == 0) ? 0.0 : dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }

    private static Map<String, Integer> termFrequency(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        Map<String, Integer> tf = new HashMap<>();
        for (String w : words) {
            if (!w.isBlank()) {
                tf.merge(w, 1, Integer::sum);
            }
        }
        return tf;
    }

    public static void main(String[] args) throws IOException {

        var resource1 = new ClassPathResource("donate/donate.txt");
        String f1 = new String(resource1.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        var resource2 = new ClassPathResource("instructions/instructionV4.txt");
        String f2 = new String(resource2.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        double score = SimilarityUtil.similarity(f1, f2);
        System.out.println("Similarity: " + score);

    }
}
