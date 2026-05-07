package com.example.demo.modules.community.forum.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FormRequest {
	String tieuDe;
	String tags;
	String content;
	String contentId; // Existing quiz ID (optional)
}
