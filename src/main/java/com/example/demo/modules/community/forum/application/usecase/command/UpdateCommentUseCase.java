package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.CommentRequest;
import com.example.demo.modules.community.forum.api.dto.CommentResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Cập nhật nội dung bình luận của người dùng hiện tại.
 *
 * <p>Kiểm tra quyền sở hữu bình luận thông qua SecurityContext.
 * Nếu hợp lệ, cập nhật nội dung và gán cờ hasChanged = true.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UpdateCommentUseCase {

    private final CommentRepository commentRepository;

    /**
     * Cập nhật bình luận dựa trên ID.
     *
     * @param commentId ID của bình luận
     * @param request dữ liệu yêu cầu cập nhật
     * @return StateResponse chứa CommentResponse cập nhật
     * @throws RuntimeException nếu không tìm thấy bình luận
     * @throws AccessDeniedException nếu người dùng không phải chủ sở hữu
     */
    @Transactional
    public StateResponse<Object> execute(String commentId, CommentRequest request) {
        Long id = Long.parseLong(commentId);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        if (!comment.getUser().getUserName().equals(currentUsername)) {
            throw new AccessDeniedException("You don't have permission to update this comment");
        }

        comment.setNoiDung(request.getNoiDung());
        comment.setHasChanged(true);
        commentRepository.save(comment);

        return StateResponse.builder()
                .result(CommentResponse.builder()
                        .id(comment.getCommenttId())
                        .tacGia(comment.getUser().getUserName())
                        .noiDung(comment.getNoiDung())
                        .ngayComment(comment.getNgayComment())
                        .hasChanged(comment.getHasChanged())
                        .isAuthor(true)
                        .build())
                .message("Cập nhật bình luận thành công")
                .build();
    }
}
