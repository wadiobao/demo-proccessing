package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

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
import com.example.demo.dto.form.FormRequest;
import com.example.demo.dto.form.FormResponse;
import com.example.demo.dto.form.TopicRequest;
import com.example.demo.service.FormService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = FormController.class)
@Import(TestSecurityConfig.class)
class FormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FormService formService;

    private StateResponse<Object> successResponse;
    private FormRequest formRequest;
    private TopicRequest topicRequest;

    @BeforeEach
    void setUp() {
        successResponse = StateResponse.builder().code(1000).build();
        formRequest = FormRequest.builder().tieuDe("Test Form").content("Test Content").build();
        topicRequest = TopicRequest.builder().topic("Test Topic").build();
    }

    @Test
    void getAllTopic_ShouldReturnSuccess() throws Exception {
        List<String> topics = Arrays.asList("Topic1", "Topic2");
        when(formService.getAllTopics()).thenReturn(StateResponse.builder().code(1000).result(topics).build());

        mockMvc.perform(MockMvcRequestBuilders.get("/discussion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0]").value("Topic1"));
    }

    @Test
    void getAllForm_ShouldReturnSuccess() throws Exception {
        Long topicId = 1L;
        List<FormResponse> forms = Arrays.asList(FormResponse.builder().tieuDe("Form1").build());
        when(formService.getAllFormFromTopic(any(Long.class))).thenReturn(StateResponse.builder().code(1000).result(forms).build());

        mockMvc.perform(MockMvcRequestBuilders.get("/discussion/" + topicId + "/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].tieuDe").value("Form1"));
    }

    @Test
    void newForm_ValidRequest_ShouldReturnSuccess() throws Exception {
        Long topicId = 1L;
        when(formService.newForm(any(Long.class), any(FormRequest.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/discussion/" + topicId + "/newform")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getFormComment_ValidId_ShouldReturnSuccess() throws Exception {
        String formId = "testFormId";
        when(formService.getFormComment(any(String.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/discussion/form/" + formId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void newTopic_ValidRequest_ShouldReturnSuccess() throws Exception {
        when(formService.newTopic(any(TopicRequest.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/discussion/newtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteForm_ValidId_ShouldReturnSuccess() throws Exception {
        String formId = "testFormId";
        when(formService.deleteForm(any(String.class))).thenReturn(successResponse);

        mockMvc.perform(MockMvcRequestBuilders.delete("/discussion/delete/" + formId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
