package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.dto.StateResponse;
import com.example.demo.dto.form.CommentRequest;
import com.example.demo.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CommentController.class)
@Import(TestSecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private CommentRequest commentRequest;
    private StateResponse<Object> successResponse;

    @BeforeEach
    void setUp() {
        commentRequest = CommentRequest.builder()
                .noiDung("Test comment content")
                .build();
        successResponse = StateResponse.builder().code(1000).build();
    }

    @Test
    void newComment_ValidRequest_ShouldReturnSuccess() throws Exception {
        String formId = "testFormId";
        when(commentService.newComment(any(String.class), any(CommentRequest.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/discussion/comment/" + formId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void deleteComment_ValidId_ShouldReturnSuccess() throws Exception {
        String commentId = "testCommentId";
        when(commentService.deleteComment(any(String.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.delete("/discussion/comment/delete/" + commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}
