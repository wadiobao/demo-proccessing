package com.example.demo.modules.community.forum.application.usecase.command;

import java.util.Date;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.CommentRequest;
import com.example.demo.modules.community.forum.api.dto.CommentResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.persistence.mapper.IdentityEntityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddCommentUseCase {

        private final CommentRepository commentRepository;
        private final IUserRepository userRepository;
        private final FormRepository formRepository;
        private final IdentityEntityMapper identityMapper;

        @Transactional
        public StateResponse<Object> execute(String formId, CommentRequest request) {
                var context = SecurityContextHolder.getContext();
                String name = context.getAuthentication().getName();

                User user = userRepository.findByUserName(name)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Form form = formRepository.findById(formId)
                                .orElseThrow(() -> new RuntimeException("Form not found"));

                Comment comment = Comment.builder()
                                .user(identityMapper.toEntity(user))
                                .noiDung(request.getNoiDung())
                                .hasChanged(false)
                                .form(form)
                                .ngayComment(new Date())
                                .build();
                commentRepository.save(comment);

                return StateResponse.builder()
                                .result(CommentResponse.builder()
                                                .tacGia(name)
                                                .noiDung(comment.getNoiDung())
                                                .ngayComment(comment.getNgayComment())
                                                .hasChanged(comment.getHasChanged())
                                                .isAuthor(true)
                                                .build())
                                .build();
        }
}
