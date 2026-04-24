package com.example.demo.modules.document.shared.domain.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicAndTags {
    // ID duy nhất bằng tiếng Anh (ví dụ: 'machine-learning')
    private String topicId;
    
    // Tên hiển thị bằng ngôn ngữ gốc (ví dụ: 'Học máy')
    private String topicDisplay;
    
    // Danh sách các tag trích xuất được
    private List<String> tags;
    
    // Ngôn ngữ của tài liệu (vi, en, jp...)
    private String language;
}
