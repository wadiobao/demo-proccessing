package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCommentUseCase {

    private final CommentRepository commentRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StateResponse<Object> execute(String commentId) {
        Long id = Long.parseLong(commentId);
        commentRepository.deleteById(id);
        
        if (commentRepository.findById(id).isEmpty()) {
            return StateResponse.builder().message("Xóa thành công").build();
        }

        return StateResponse.builder().message("Xóa không thành công").build();
    }
}
