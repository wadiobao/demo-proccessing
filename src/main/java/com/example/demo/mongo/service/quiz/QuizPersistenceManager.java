package com.example.demo.mongo.service.quiz;

import org.springframework.stereotype.Component;

import com.example.demo.mongo.dto.question.FileGenerateResponse;
import com.example.demo.mongo.entity.ArchivedQuestion;
import com.example.demo.mongo.entity.Content;
import com.example.demo.mongo.service.ArchivedQuestionService;
import com.example.demo.mongo.service.iservice.IContentService;
import com.example.demo.mongo.service.iservice.IUserResourceService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages persistence of quiz-related data.
 * Handles the correct order of saving: Content -> UserResource ->
 * ArchivedQuestion
 * Follows Single Responsibility Principle.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuizPersistenceManager  {

    IContentService contentService;
    IUserResourceService userResourceService;
    ArchivedQuestionService archivedQuestionService;

    /**
     * Persists all quiz-related data for authenticated users.
     * 
     * @param response   Generated quiz response
     * @param username   Authenticated user's username
     * @param filename   Original PDF filename
     * @param pdfContent Extracted text from PDF
     * @throws Exception if persistence fails
     */
    @Transactional
    public void persistQuizData(FileGenerateResponse response, String username, String filename, String pdfContent)
            throws Exception {

        log.info("Starting quiz data persistence for user: {}, file: {}", username, filename);

        // Step 1: Save Content (with AI-generated metadata)
        Content content = contentService.save(pdfContent, username);
        log.debug("Content saved with ID: {}", content.getId());

        // Step 2: Save UserResource (linked to Content via topic)
        userResourceService.save(filename, pdfContent, username, content);
        log.debug("UserResource updated for topic: {}", content.getTopic());

        // Step 3: Save ArchivedQuestion (linked to Content via resourceId)
        ArchivedQuestion archivedQuestion = ArchivedQuestion.builder()
                .author(username)
                .content(response.getQuestions())
                .pdfBase64(response.getPdfBase64())
                .wordBase64(response.getWordBase64())
                .title(filename)
                .resourceId(content.getId())
                .build();

        archivedQuestionService.save(archivedQuestion);
        log.info("Quiz data persistence completed successfully for user: {}", username);
    }
}
