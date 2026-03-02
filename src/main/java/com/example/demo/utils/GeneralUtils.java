package com.example.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * General utility helper class for cross-cutting concerns.
 * 
 * <p>
 * Cung cấp các phương thức hỗ trợ chung cho toàn hệ thống,
 * bao gồm các xử lý toán học hoặc thuật toán cơ bản.
 *
 * @since 1.0
 */
@Component
public class GeneralUtils {

    /**
     * Generates a SHA-256 hex string from the input text.
     * 
     * <p>
     * Chuyển đổi văn bản đầu vào thành chuỗi băm SHA-256 định dạng hex
     * phục vụ cho việc kiểm tra tính toàn vẹn hoặc định danh dữ liệu.
     *
     * @param input raw string to be hashed / chuỗi văn bản cần băm
     * @return hex encoded hash string / chuỗi băm định dạng hex
     * @throws RuntimeException if hashing algorithm is not found / lỗi nếu thuật
     *                          toán không khả dụng
     */
    public String sha256(String input) {
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
}
