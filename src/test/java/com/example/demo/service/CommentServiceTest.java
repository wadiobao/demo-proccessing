package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.CommentRequest;
import com.example.demo.sql.dto.form.CommentResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.entity.Comment;
import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.CommentRepository;
import com.example.demo.sql.repository.FormRepository;
import com.example.demo.sql.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FormRepository formRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Form form;
    private CommentRequest commentRequest;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = User.builder().userName("testuser").build();
        form = Form.builder().formId("testFormId").build();
        commentRequest = CommentRequest.builder().noiDung("Test comment content").build();
        comment = Comment.builder()
                .user(user)
                .noiDung(commentRequest.getNoiDung())
                .form(form)
                .ngayComment(new Date())
                .build();

        // Mock SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void newComment_ValidRequest_ShouldReturnSuccessResponse() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));
        when(formRepository.findById(anyString())).thenReturn(Optional.of(form));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        StateResponse<Object> response = commentService.newComment("testFormId", commentRequest);

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof CommentResponse);
        CommentResponse commentResponse = (CommentResponse) response.getResult();
        assertEquals("testuser", commentResponse.getTacGia());
        assertEquals("Test comment content", commentResponse.getNoiDung());
        verify(userRepository, times(1)).findByUserName(anyString());
        verify(formRepository, times(1)).findById(anyString());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void newComment_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.newComment("testFormId", commentRequest));
        verify(userRepository, times(1)).findByUserName(anyString());
        verify(formRepository, never()).findById(anyString());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void newComment_FormNotFound_ShouldThrowException() {
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));
        when(formRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.newComment("testFormId", commentRequest));
        verify(userRepository, times(1)).findByUserName(anyString());
        verify(formRepository, times(1)).findById(anyString());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteComment_SuccessfulDeletion_ShouldReturnSuccessMessage() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty()); // Simulate successful deletion

        StateResponse<Object> response = commentService.deleteComment("123");

        assertNotNull(response);
        assertEquals("Xóa thành công", response.getMessage());
        verify(commentRepository, times(1)).deleteById(123L);
        verify(commentRepository, times(1)).findById(123L);
    }

    @Test
    void deleteComment_DeletionFailed_ShouldReturnFailureMessage() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment)); // Simulate deletion failure

        StateResponse<Object> response = commentService.deleteComment("123");

        assertNotNull(response);
        assertEquals("Xóa không thành công", response.getMessage());
        verify(commentRepository, times(1)).deleteById(123L);
        verify(commentRepository, times(1)).findById(123L);
    }
}
