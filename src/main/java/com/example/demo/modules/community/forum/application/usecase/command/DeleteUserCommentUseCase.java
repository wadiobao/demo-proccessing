package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Cho phép người dùng hiện tại xóa bình luận của chính mình.
 *
 * <p>Xác thực người dùng qua SecurityContext và so sánh với chủ sở hữu bình luận.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class DeleteUserCommentUseCase {

    private final CommentRepository commentRepository;

    /**
     * Xóa bình luận của người dùng hiện tại dựa trên ID.
     *
     * @param commentId ID của bình luận
     * @return StateResponse thông báo kết quả
     * @throws RuntimeException nếu không tìm thấy bình luận
     * @throws AccessDeniedException nếu người dùng không phải chủ sở hữu
     */
    @Transactional
    public StateResponse<Object> execute(String commentId) {
        Long id = Long.parseLong(commentId);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        if (!comment.getUser().getUserName().equals(currentUsername)) {
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);

        return StateResponse.builder().message("Xóa bình luận thành công").build();
    }
}
