package com.example.demo.modules.document.retrieval.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.modules.document.retrieval.application.usecase.GenerateExcelQuizUseCase;
import com.example.demo.modules.document.retrieval.application.usecase.GenerateQuizDocumentUseCase;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.RequiredArgsConstructor;

/**
 * Public Facade for the Document Retrieval module.
 * 
 * <p>
 * Cung cấp các công cụ trích xuất và xuất bản tài liệu cho các module khác.
 */
@Component
@RequiredArgsConstructor
public class DocumentRetrievalFacade {

    private final GenerateQuizDocumentUseCase generateQuizDocumentUseCase;
    private final GenerateExcelQuizUseCase generateExcelQuizUseCase;

    /**
     * Generates Word and PDF versions of a quiz based on a list of questions.
     * 
     * @param questions the quiz content
     * @return array [wordBase64, pdfBase64]
     */
    public String[] generateQuizDocuments(List<Question> questions) {
        return generateQuizDocumentUseCase.execute(questions);
    }

    /**
     * Generates an Excel version of a quiz based on a list of questions.
     *
     * @param questions the quiz content
     * @return Base64 encoded Excel string
     */
    public String generateExcelDocument(List<Question> questions) {
        return generateExcelQuizUseCase.execute(questions);
    }
}
