package com.example.demo.mongo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.mongo.dto.quiz.QuizConfig;
import com.example.demo.mongo.dto.quiz.QuizSubmissionRequest;
import com.example.demo.mongo.service.BulkQuestionUploadService;
import com.example.demo.mongo.service.iservice.IQuizAnswerService;
import com.example.demo.mongo.service.iservice.IQuizService;
import com.example.demo.mongo.service.iservice.IUserAnalyticsService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * REST Controller for quiz generation operations.
 * Delegates to QuizService for business logic.
 */
@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizController {

    IQuizService quizService;
    IQuizAnswerService quizAnswerService;
    IUserAnalyticsService userAnalyticsService;
    BulkQuestionUploadService bulkQuestionUploadService;

    /**
     * Generates a quiz for public (unauthenticated) users.
     * No data is persisted.
     *
     * @param file          PDF file to process
     * @param questionCount Number of questions to generate
     * @param level         Difficulty level (0=Easy, 1=Hard, 2=Adaptive)
     * @param type          Knowledge type (0=Memorization, 1=Application)
     * @param language      Language for questions
     * @param imgQuest      Generate images (0=No, 1=Yes)
     * @return StateResponse containing the generated quiz
     */
    @PostMapping("/public")
    public ResponseEntity<StateResponse<Object>> generatePublicQuiz(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int questionCount,
            @RequestParam(defaultValue = "0") int level,
            @RequestParam(defaultValue = "0") int type,
            @RequestParam(defaultValue = "vietnamese") String language,
            @RequestParam(defaultValue = "0") int imgQuest) {

        QuizConfig config = QuizConfig.builder()
                .questionCount(questionCount)
                .level(level)
                .type(type)
                .language(language)
                .imgQuest(imgQuest)
                .build();

        StateResponse<Object> response = quizService.processPublicQuiz(file, config);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a quiz for authenticated users.
     * Quiz data is persisted to the database.
     *
     * @param file          PDF file to process
     * @param questionCount Number of questions to generate
     * @param level         Difficulty level (0=Easy, 1=Hard, 2=Adaptive)
     * @param type          Knowledge type (0=Memorization, 1=Application)
     * @param language      Language for questions
     * @param imgQuest      Generate images (0=No, 1=Yes)
     * @return StateResponse containing the generated quiz
     * @throws Exception if processing or persistence fails
     */
    @PostMapping("/private")
    public ResponseEntity<StateResponse<Object>> generatePrivateQuiz(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int questionCount,
            @RequestParam(defaultValue = "0") int level,
            @RequestParam(defaultValue = "0") int type,
            @RequestParam(defaultValue = "vietnamese") String language,
            @RequestParam(defaultValue = "0") int imgQuest,
            @RequestParam(required = false) String topic) throws Exception {

        QuizConfig config = QuizConfig.builder()
                .questionCount(questionCount)
                .level(level)
                .type(type)
                .language(language)
                .imgQuest(imgQuest)
                .topic(topic)
                .build();

        StateResponse<Object> response = quizService.processPrivateQuiz(file, config);
        return ResponseEntity.ok(response);
    }

    /**
     * Submits quiz answers for evaluation.
     * Calculates score and updates user's IRT parameters.
     *
     * @param request Quiz submission with user answers
     * @return StateResponse containing results and updated parameters
     * @throws Exception if processing fails
     */
    @PostMapping("/submit")
    public ResponseEntity<StateResponse<Object>> submitQuizAnswers(
            @RequestBody QuizSubmissionRequest request) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = quizAnswerService.submitQuizAnswers(request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves user's learning statistics for a specific topic.
     *
     * @param topic Topic/subject to get stats for
     * @return StateResponse containing user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<StateResponse<Object>> getUserStats(
            @RequestParam String topic) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = userAnalyticsService.getUserStats(username, topic);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves global user overview statistics for the learning dashboard.
     *
     * @return StateResponse containing comprehensive user statistics
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<StateResponse<Object>> getUserOverviewStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = userAnalyticsService.getOverviewStats(username);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/analyze-questions")
	public ResponseEntity<StateResponse<Object>> uploadQuestions(@RequestParam("file") MultipartFile file,
			@RequestParam("sessionId") String sessionId) throws Exception {
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		bulkQuestionUploadService.stageQuestions(file, username, sessionId);
		return ResponseEntity.ok(StateResponse.builder().message("Questions staged successfully").build());
	}
}
