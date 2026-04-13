package com.example.demo.modules.document.metadata.domain.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Domain model representing semantic metadata of a document.
 * 
 * <p>
 * Lưu trữ thông tin về chủ đề (topic), từ khóa (tags) và 
 * các vector embeddings phục vụ cho tìm kiếm tương đồng.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentMetadata {
    String id;
    String content;
    String owner;
    List<String> tags;
    List<Double> embedding;
    String topic;
    
    // Transient field for search scoring
    Double vectorSearchScore;
}
