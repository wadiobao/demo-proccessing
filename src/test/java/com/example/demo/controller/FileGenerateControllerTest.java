package com.example.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.dto.StateResponse;
import com.example.demo.service.FileGenerateService123;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = FileGenerateController.class)
@Import(TestSecurityConfig.class)
class FileGenerateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileGenerateService123 fileGenerateService123;

    private StateResponse<Object> successResponse;

    @BeforeEach
    void setUp() {
        successResponse = StateResponse.builder().code(1000).result("Test result").build();
    }

    @Test
    void generateWord_ShouldReturnSuccess() throws Exception {
        when(fileGenerateService123.generateWordAndPdfBase64()).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/file/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").value("Test result"));
    }
}
