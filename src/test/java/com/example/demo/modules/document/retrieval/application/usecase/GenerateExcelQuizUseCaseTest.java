package com.example.demo.modules.document.retrieval.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.modules.quiz.shared.domain.model.Answer;
import com.example.demo.modules.quiz.shared.domain.model.Question;

/**
 * Unit tests verifying excel generation of quiz questions.
 *
 * @since 1.0.0
 */
class GenerateExcelQuizUseCaseTest {

    private final GenerateExcelQuizUseCase useCase = new GenerateExcelQuizUseCase();

    /**
     * Verifies that executing with null or empty questions returns null.
     */
    @Test
    void testExecuteWithNullOrEmptyQuestionsReturnsNull() {
        assertNull(useCase.execute(null));
        assertNull(useCase.execute(new ArrayList<>()));
    }

    /**
     * Verifies that valid questions produce a non-null, valid Base64 encoded string.
     */
    @Test
    void testExecuteWithValidQuestionsProducesBase64() {
        List<Question> questions = new ArrayList<>();
        
        Answer answer = Answer.builder()
                .option1("Java")
                .option2("Python")
                .option3("C++")
                .option4("Rust")
                .correctAnswer("A")
                .explanation("Java is a high-level, class-based, object-oriented programming language.")
                .build();

        questions.add(Question.builder()
                .id(1)
                .question("What is the primary language used in Spring Boot?")
                .answer(answer)
                .bloomLevel("Remember")
                .reference("Spring Boot Reference Documentation")
                .build());

        String base64Result = useCase.execute(questions);

        assertNotNull(base64Result);
        
        // Check if the output is a valid Base64 string.
        byte[] decodedBytes = Base64.getDecoder().decode(base64Result);
        assertTrue(decodedBytes.length > 0);
    }
}
