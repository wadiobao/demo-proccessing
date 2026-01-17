package com.example.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class TestRedis {
    public static void connectBasic() {
        RedisURI uri = RedisURI.Builder
                .redis("redis-16288.c241.us-east-1-4.ec2.cloud.redislabs.com", 16288)
                .withAuthentication("default", "7AqBov91NYgoc4SxnIrFyl3S0Jb7BiVh")
                .build();
        RedisClient client = RedisClient.create(uri);
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisCommands<String, String> commands = connection.sync();

        commands.set("foo", "bar");
        String result = commands.get("foo");
        System.out.println(result); // >>> bar

        connection.close();

        client.shutdown();
    }
    
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
    
    public static void main(String[] args) {
        // 1. Khởi tạo mô hình (Tự động tải model về máy trong lần đầu)
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // 2. Câu văn bản bạn muốn chuyển đổi
        String text = "Lập trình Java rất thú vị";

        // 3. Tạo Vector (Embedding)
        Embedding embedding = embeddingModel.embed(text).content();
        

        // 4. In kết quả ra màn hình
        float[] vector = embedding.vector();
        System.out.println("Độ dài vector: " + vector.length);
        for (int i = 0; i < 5; i++) { // In thử 5 số đầu tiên
            System.out.print(vector[i] + ", ");
        }
        
        
    }
}
