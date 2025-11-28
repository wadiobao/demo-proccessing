package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dto.user.UserRequest;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.enums.Role;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userName("testuser")
                .password("encodedpassword")
                .email("test@example.com")
                .date(new Date())
                .roles(new HashSet<>(Set.of(Role.USER.name())))
                .build();

        userRequest = UserRequest.builder()
                .userName("testuser")
                .password("rawpassword")
                .email("test@example.com")
                .date(new Date())
                .build();
    }

    @Test
    void getAll_ShouldReturnListOfUsers() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getUserName(), result.get(0).getUserName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void registerUser_ValidRequest_ShouldReturnUserResponse() {
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.registerUser(userRequest);

        assertNotNull(result);
        assertEquals(userRequest.getUserName(), result.getUserName());
        assertTrue(result.getRoles().contains(Role.USER.name()));
        verify(userRepository, times(1)).existsByUserName(userRequest.getUserName());
        verify(passwordEncoder, times(1)).encode(userRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_UserExisted_ShouldThrowHandleException() {
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        HandleException exception = assertThrows(HandleException.class, () -> userService.registerUser(userRequest));

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        verify(userRepository, times(1)).existsByUserName(userRequest.getUserName());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getInfor_UserExists_ShouldReturnUserResponse() {
        when(userRepository.existsByUserName(anyString())).thenReturn(true);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));

        UserResponse result = userService.getInfor("testuser");

        assertNotNull(result);
        assertEquals(user.getUserName(), result.getUserName());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).existsByUserName("testuser");
        verify(userRepository, times(1)).findByUserName("testuser");
    }

    @Test
    void getInfor_UserDoesNotExist_ShouldThrowHandleException() {
        when(userRepository.existsByUserName(anyString())).thenReturn(false);

        HandleException exception = assertThrows(HandleException.class, () -> userService.getInfor("nonexistentuser"));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, times(1)).existsByUserName("nonexistentuser");
        verify(userRepository, never()).findByUserName(anyString());
    }

    @Test
    void myInfor_UserFound_ShouldReturnUserResponse() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(user));

        UserResponse result = userService.myInfor();

        assertNotNull(result);
        assertEquals(user.getUserName(), result.getUserName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRoles(), result.getRoles());
        verify(userRepository, times(1)).findByUserName("testuser");
    }

    @Test
    void myInfor_UserNotFound_ShouldThrowHandleException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistentuser");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        HandleException exception = assertThrows(HandleException.class, () -> userService.myInfor());

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository, times(1)).findByUserName("nonexistentuser");
    }
}
