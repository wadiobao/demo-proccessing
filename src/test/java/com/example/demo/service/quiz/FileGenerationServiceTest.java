package com.example.demo.service.quiz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.question.Answer;
import com.example.demo.dto.question.Question;
import com.example.demo.utils.FileGeneratorUtils;

@ExtendWith(MockitoExtension.class)
class FileGenerationServiceTest {

    @Mock
    private FileGeneratorUtils fileGeneratorUtils;

    @InjectMocks
    private WordPdfGeneration fileGenerationService;

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
                .explain("Explanation 1")
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
                .explain("Explanation 2")
                .build();
        question2 = Question.builder()
                .id(2)
                .question("Question 2")
                .answer(answer2)
                .build();
    }

    @Test
    void generateWordAndPdfBase64_ValidQuestions_ShouldReturnBase64Strings() throws Exception {
        List<Question> questions = Arrays.asList(question1, question2);
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenReturn("wordBase64");
        when(fileGeneratorUtils.exportPdfBase64(anyString(), anyMap())).thenReturn("pdfBase64");

        String[] result = fileGenerationService.generateWordAndPdfBase64(questions);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("wordBase64", result[0]);
        assertEquals("pdfBase64", result[1]);
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(anyString(), anyMap());
        verify(fileGeneratorUtils, times(1)).exportPdfBase64(anyString(), anyMap());
    }

    @Test
    void generateWordAndPdfBase64_NullQuestions_ShouldReturnNull() {
        String[] result = fileGenerationService.generateWordAndPdfBase64(null);
        assertNull(result);
        verifyNoInteractions(fileGeneratorUtils);
    }

    @Test
    void generateWordAndPdfBase64_EmptyQuestions_ShouldReturnNull() {
        String[] result = fileGenerationService.generateWordAndPdfBase64(new ArrayList<>());
        assertNull(result);
        verifyNoInteractions(fileGeneratorUtils);
    }

    @Test
    void generateWordAndPdfBase64_QuestionWithNullAnswer_ShouldSkipQuestion() throws Exception {
        Question questionWithNullAnswer = Question.builder().id(3).question("Question 3").answer(null).build();
        List<Question> questions = Arrays.asList(question1, questionWithNullAnswer);

        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenReturn("wordBase64");
        when(fileGeneratorUtils.exportPdfBase64(anyString(), anyMap())).thenReturn("pdfBase64");

        String[] result = fileGenerationService.generateWordAndPdfBase64(questions);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("wordBase64", result[0]);
        assertEquals("pdfBase64", result[1]);
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(anyString(), anyMap());
        verify(fileGeneratorUtils, times(1)).exportPdfBase64(anyString(), anyMap());
    }

    @Test
    void generateWordAndPdfBase64_FileGeneratorUtilsThrowsException_ShouldReturnNull() throws Exception {
        List<Question> questions = Arrays.asList(question1);
        when(fileGeneratorUtils.generateWordFromTemplateReturnBase64(anyString(), anyMap())).thenThrow(new RuntimeException("File generation error"));

        String[] result = fileGenerationService.generateWordAndPdfBase64(questions);

        assertNull(result);
        verify(fileGeneratorUtils, times(1)).generateWordFromTemplateReturnBase64(anyString(), anyMap());
        verify(fileGeneratorUtils, never()).exportPdfBase64(anyString(), anyMap());
    }
}
