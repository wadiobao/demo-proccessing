package com.example.demo.modules.community.forum.api.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Detailed response for a form, including its questions and paginated comments.
 * 
 * <p>Đóng gói danh sách câu hỏi (từ MongoDB) và phân trang các bình luận (từ SQL)
 * để cung cấp chi tiết đầy đủ của một cuộc thảo luận.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FormDetailResponse {
    FormResponse form;
    List<Question> questions;
    Page<CommentResponse> comments;
}
