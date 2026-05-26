package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormContentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;

import lombok.RequiredArgsConstructor;

/**
 * Xóa form của tác giả hiện tại.
 *
 * <p>Kiểm tra quyền sở hữu bằng cách so sánh tác giả của form với người dùng hiện tại.
 * Sau đó tiến hành xóa lần lượt comment, content và form để tránh lỗi khóa ngoại.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class DeleteUserFormUseCase {

    private final FormRepository formRepository;
    private final FormContentRepository contentRepository;
    private final CommentRepository commentRepository;

    /**
     * Xóa form dựa trên ID nếu thuộc sở hữu của người dùng.
     *
     * @param formId ID của form
     * @return StateResponse thông báo kết quả
     * @throws RuntimeException nếu không tìm thấy form
     * @throws AccessDeniedException nếu người dùng không phải tác giả
     */
    @Transactional
    public StateResponse<Object> execute(String formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        if (!form.getTacGia().equals(currentUsername)) {
            throw new AccessDeniedException("You don't have permission to delete this form");
        }

        formRepository.deleteById(formId);
        contentRepository.deleteById(formId);
        commentRepository.deleteAllByFormId(formId);

        return StateResponse.builder().message("Xóa thành công form của bạn").build();
    }
}
