package com.example.demo.modules.community.forum.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.CommentResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminGetAllCommentsUseCase {

    private final CommentRepository commentRepository;
    private final IUserRepository userRepository;

    /**
     * Retrieve all comments ordered by creation date descending with pagination.
     * Decorated with author details and ownership flag for the requesting admin.
     * 
     * @param pageable pagination options
     * @return StateResponse containing the paginated list of comment responses
     */
    public StateResponse<Object> execute(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByOrderByNgayCommentDesc(pageable);
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

        Page<CommentResponse> responses = comments.map(comment -> {
            boolean isCommentAuthor = currentUser != null 
                    && comment.getUser() != null 
                    && comment.getUser().getUserName().equals(currentUser.getUserName());
            
            return CommentResponse.builder()
                    .id(comment.getCommenttId())
                    .tacGia(comment.getUser() != null ? comment.getUser().getUserName() : "Unknown")
                    .noiDung(comment.getNoiDung())
                    .hasChanged(comment.getHasChanged())
                    .isAuthor(isCommentAuthor)
                    .ngayComment(comment.getNgayComment())
                    .build();
        });

        return StateResponse.builder().result(responses).build();
    }
}
