package com.example.demo.sql.service.iservice;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.CommentRequest;

public interface ICommentService {
    StateResponse<Object> newComment(String formId, CommentRequest request);

    StateResponse<Object> deleteComment(String commentId);
}
