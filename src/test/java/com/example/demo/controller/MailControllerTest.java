package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.dto.StateResponse;
import com.example.demo.service.OTPMailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MailController.class)
@Import(TestSecurityConfig.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OTPMailService mailService;

    private StateResponse<Object> successResponse;

    @BeforeEach
    void setUp() {
        successResponse = StateResponse.builder().code(1000).build();
    }

    @Test
    void sendMail_ValidRequest_ShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        when(mailService.sendDonatetoMyMail(anyString(), anyString(), any())).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/mail/donate")
                .file(file)
                .param("name", "Test User")
                .param("note", "Test Note"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void sendBug_ValidRequest_ShouldReturnSuccess() throws Exception {
        when(mailService.sendBugtoMyMail(anyString(), anyString())).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/mail/send-bug")
                .param("name", "Bug Reporter")
                .param("note", "Found a bug!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}
