package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormContentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteFormUseCase {

    private final FormRepository formRepository;
    private final FormContentRepository contentRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public StateResponse<Object> execute(String formId) {
        formRepository.deleteById(formId);
        contentRepository.deleteById(formId);
        commentRepository.deleteAllByFormId(formId);

        return StateResponse.builder().message("Xóa thành công form").build();
    }
}
