package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.question.Answer;
import com.example.demo.dto.question.FileGenerateResponse;
import com.example.demo.dto.question.Question;
import com.example.demo.dto.question.UserQuestion;
import com.example.demo.utils.FileGeneratorUtils;

@ExtendWith(MockitoExtension.class)
class FileGenerateService123Test {

    @Mock
    private FileGeneratorUtils fileGeneratorUtils;

    @Mock
    private UserQuestion userQuestion;

    @InjectMocks
    private FileGenerateService123 fileGenerateService123;

    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        Answer answer1 = Answer.builder()
                .A("Option A1")
                .B("Option B1")
                .C("Option C1")
                .D("Option D1")
                .correct("A")
                .build();
        question1 = Question.builder()
                .id(1)
                .question("Question 1")
                .answer(answer1)
                .build();

        Answer answer2 = Answer.builder()
                .A("Option A2")
                .B("Option B2")
                .C("Option C2")
                .D("Option D2")
                .correct("B")
                .build();
        question2 = Question.builder()
                .id(2)
                .question("Question 2")
                .answer(answer2)
                .build();
    }

    @Test
    void generateWordAndPdfBase64_ValidQuestions_ShouldReturnSuccessResponse() throws Exception {
        when(userQuestion.getQuestionList()).thenReturn(Arrays.asList(question1, question2));
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenReturn("wordBase64");
        when(fileGeneratorUtils.exportPdfBase64(anyString(), anyMap())).thenReturn("pdfBase64");

        StateResponse<Object> response = fileGenerateService123.generateWordAndPdfBase64();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof FileGenerateResponse);
        FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();
        assertEquals("wordBase64", fileGenerateResponse.getWordBase64());
        assertEquals("pdfBase64", fileGenerateResponse.getPdfBase64());

        verify(userQuestion, times(1)).getQuestionList();
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(eq("/templates/docx_template.docx"), anyMap());
        verify(fileGeneratorUtils, times(1)).exportPdfBase64(eq("/templates/docx_template.docx"), anyMap());
    }

    @Test
    void generateWordAndPdfBase64_NullUserQuestion_ShouldReturnNull() {
        when(userQuestion.getQuestionList()).thenReturn(null);

        StateResponse<Object> response = fileGenerateService123.generateWordAndPdfBase64();

        assertNull(response);
        verify(userQuestion, times(1)).getQuestionList();
        verifyNoInteractions(fileGeneratorUtils);
    }

    @Test
    void generateWordAndPdfBase64_EmptyQuestionList_ShouldReturnSuccessResponseWithEmptyData() throws Exception {
        when(userQuestion.getQuestionList()).thenReturn(new ArrayList<>());
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenReturn("wordBase64");
        when(fileGeneratorUtils.exportPdfBase64(anyString(), anyMap())).thenReturn("pdfBase64");

        StateResponse<Object> response = fileGenerateService123.generateWordAndPdfBase64();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof FileGenerateResponse);
        FileGenerateResponse fileGenerateResponse = (FileGenerateResponse) response.getResult();
        assertEquals("wordBase64", fileGenerateResponse.getWordBase64());
        assertEquals("pdfBase64", fileGenerateResponse.getPdfBase64());

        verify(userQuestion, times(1)).getQuestionList();
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(eq("/templates/docx_template.docx"), anyMap());
        verify(fileGeneratorUtils, times(1)).exportPdfBase64(eq("/templates/docx_template.docx"), anyMap());
    }

    @Test
    void generateWordAndPdfBase64_QuestionWithNullAnswer_ShouldSkipQuestion() throws Exception {
        Question questionWithNullAnswer = Question.builder().id(3).question("Question 3").answer(null).build();
        when(userQuestion.getQuestionList()).thenReturn(Arrays.asList(question1, questionWithNullAnswer));
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenReturn("wordBase64");
        when(fileGeneratorUtils.exportPdfBase64(anyString(), anyMap())).thenReturn("pdfBase64");

        StateResponse<Object> response = fileGenerateService123.generateWordAndPdfBase64();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof FileGenerateResponse);

        verify(userQuestion, times(1)).getQuestionList();
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(eq("/templates/docx_template.docx"), anyMap());
        verify(fileGeneratorUtils, times(1)).exportPdfBase64(eq("/templates/docx_template.docx"), anyMap());
    }

    @Test
    void generateWordAndPdfBase64_FileGeneratorUtilsThrowsException_ShouldReturnNull() throws Exception {
        when(userQuestion.getQuestionList()).thenReturn(Arrays.asList(question1));
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenThrow(new RuntimeException("File generation error"));

        StateResponse<Object> response = fileGenerateService123.generateWordAndPdfBase64();

        assertNull(response);
        verify(userQuestion, times(1)).getQuestionList();
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(eq("/templates/docx_template.docx"), anyMap());
        verify(fileGeneratorUtils, never()).exportPdfBase64(anyString(), anyMap());
    }
}
