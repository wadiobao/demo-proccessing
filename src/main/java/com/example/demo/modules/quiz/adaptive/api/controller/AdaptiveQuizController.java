package com.example.demo.modules.quiz.adaptive.api.controller;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.adaptive.api.AdaptiveQuizFacade;
import com.example.demo.modules.quiz.adaptive.api.request.QuizSubmissionRequest;
import com.example.demo.modules.quiz.shared.domain.model.QuizConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for Adaptive (Private) quiz operations for authenticated users.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdaptiveQuizController {

    AdaptiveQuizFacade adaptiveQuizFacade;
    StringRedisTemplate redisTemplate;
    DefaultRedisScript<String> lockAndCheckScript;
    DefaultRedisScript<Long> safeUnlockScript;
    ObjectMapper mapper;

    /**
     * Generates a quiz for authenticated users with adaptive difficulty.
     */
    @PostMapping("/private")
    public ResponseEntity<StateResponse<Object>> generatePrivateQuiz(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "15") int questionCount,
            @RequestParam(defaultValue = "vietnamese") String language,
            @RequestParam(required = false) String topic) throws Exception {

        QuizConfig config = QuizConfig.builder()
                .questionCount(questionCount)
                .level(2) // Adaptive
                .type(1)
                .language(language)
                .topic(topic)
                .build();

        StateResponse<Object> response = adaptiveQuizFacade.generatePrivateQuiz(files, config,
                SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a review quiz for an existing topic using previously uploaded
     * files.
     */
    @PostMapping("/review")
    public ResponseEntity<StateResponse<Object>> generateReviewQuiz(
            @RequestParam String id,
            @RequestParam(required = false) Integer questionCount,
            @RequestParam(defaultValue = "vietnamese") String language) throws Exception {

        QuizConfig config = QuizConfig.builder()
                .questionCount(questionCount != null ? questionCount : 0)
                .level(2) // Adaptive
                .type(1)
                .language(language)
                .build();
        
        String contentHash = config.toString(); // Mã băm nội dung
        String dataKey = "quiz:data:" + contentHash;
        String lockKey = "quiz:lock:" + contentHash;
        String channel = "quiz:channel:" + contentHash;
        String requestId = UUID.randomUUID().toString();
        
        String status = redisTemplate.execute(lockAndCheckScript, 
                Arrays.asList(dataKey, lockKey), requestId, "180");

		if ("HAS_DATA".equals(status)) {
			String json = redisTemplate.opsForValue().get(dataKey);
			StateResponse response = mapper.readValue(json,StateResponse.class); // Trả về ngay nếu đã có trong cache
	        return ResponseEntity.ok(response);
		}
		
		if ("LOCKED".equals(status)) {
		// --- ĐÂY LÀ REQUEST A (Người thắng cuộc) ---
		try {   
				
				StateResponse<Object> response = adaptiveQuizFacade.generateReviewQuiz(id, config,
						SecurityContextHolder.getContext().getAuthentication().getName(),requestId);
				String json =  mapper.writeValueAsString(response);
				redisTemplate.opsForValue().set(dataKey, json, Duration.ofMinutes(2));
				redisTemplate.opsForList().leftPush("signal:" + dataKey, "DONE");
				redisTemplate.expire("signal:" + dataKey, Duration.ofMinutes(1));

                log.info("Primary session has been processed and persisted data");
        return ResponseEntity.ok(response);
		} finally {
            // Xóa khóa an toàn bằng Lua Script
            redisTemplate.execute(safeUnlockScript, Collections.singletonList(lockKey), requestId);
            log.info("Delete lock key");
        }
		} else {
    	
    	log.info("Other session get data from the primary session");
        // --- ĐÂY LÀ REQUEST B, C... (Người đợi) ---
    	StateResponse response = waitForSignal(dataKey);
    	
        return ResponseEntity.ok(response);

    	}
    }
    
    private StateResponse<?> waitForSignal(String dataKey) throws JsonMappingException, JsonProcessingException {
        // Tên key dùng làm "hộp thư" chờ
        String signalKey = "signal:" + dataKey; 

        // Đợi Redis đẩy tín hiệu vào List trong tối đa 200 giây
        // Lệnh này sẽ chặn thread lại nhưng cực kỳ tiết kiệm tài nguyên
        Object signal = redisTemplate.opsForList().leftPop(signalKey, 200, TimeUnit.SECONDS);

        if (signal != null) {
            String json = redisTemplate.opsForValue().get(dataKey);
            StateResponse response = mapper.readValue(json,StateResponse.class);
			return response;
        }
        throw new RuntimeException("AI processing timeout!");
    }
    
    private StateResponse<?> waitForPubSubSignal(String channel, String dataKey) throws JsonMappingException, JsonProcessingException {
        // Sử dụng CompletableFuture hoặc đơn giản hơn là vòng lặp chờ ngắn 
        // kết hợp với kiểm tra dataKey để đảm bảo an toàn.
        int retry = 0;
        while (retry < 180) { // Đợi tối đa 3p
            String data = redisTemplate.opsForValue().get(dataKey);
            if (data != null) {
            	String json = redisTemplate.opsForValue().get(dataKey);
    			StateResponse response = mapper.readValue(json,StateResponse.class); // Trả về ngay nếu đã có trong cache
    			return response;
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            retry++;
        }
        throw new RuntimeException("AI processing timeout!");
    }

    /**
     * Submits quiz answers for evaluation and IRT update.
     */
    @PostMapping("/submit")
    public ResponseEntity<StateResponse<Object>> submitQuizAnswers(
            @RequestBody QuizSubmissionRequest request) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.submitQuiz(request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves user's learning statistics for a specific topic.
     */
    @GetMapping("/stats")
    public ResponseEntity<StateResponse<Object>> getUserStats(
            @RequestParam String topic) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getUserStats(username, topic);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves global user overview statistics for the learning dashboard.
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<StateResponse<Object>> getUserOverviewStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getOverviewStats(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new learning topic with initial documents and settings.
     */
    @PostMapping("/topics")
    public ResponseEntity<StateResponse<Object>> createTopic(
            @RequestParam String topic,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(defaultValue = "15") int sessionSize) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.createTopic(topic, files, sessionSize, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing topic name.
     */
    @PutMapping("/topics/{id}")
    public ResponseEntity<StateResponse<Object>> updateTopic(
            @PathVariable String id,
            @RequestParam String topic) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.updateTopic(id, topic, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an existing topic.
     */
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<StateResponse<Object>> deleteTopic(
            @PathVariable String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.deleteTopic(id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a file to a specific learning topic for the authenticated user.
     */
    @PostMapping("/topics/files")
    public ResponseEntity<StateResponse<Object>> addFileToTopic(
            @RequestParam("file") MultipartFile file,
            @RequestParam String id) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.addFileToTopic(file, id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the list of files in a specific learning topic for the
     * authenticated user.
     */
    @GetMapping("/topics/files")
    public ResponseEntity<StateResponse<Object>> getTopicFiles(
            @RequestParam String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getTopicFiles(id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves information of all topics for the authenticated user.
     */
    @GetMapping("/topics")
    public ResponseEntity<StateResponse<Object>> getAllTopicsInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getAllTopicsInfo(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the theta score history of a specific topic for the authenticated
     * user.
     */
    @GetMapping("/topics/score-history")
    public ResponseEntity<StateResponse<Object>> getTopicScoreHistory(
            @RequestParam String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getTopicScoreHistory(id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the unified topic overview: IRT stats, ELO progress, theta history,
     * and file list — all in a single O(2)-query call.
     *
     * <p>Replaces the need to call {@code /topics/score-history} and
     * {@code /topics/files} separately.
     */
    @GetMapping("/topics/overview")
    public ResponseEntity<StateResponse<Object>> getTopicOverview(
            @RequestParam String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        StateResponse<Object> response = adaptiveQuizFacade.getTopicOverview(id, username);
        return ResponseEntity.ok(response);
    }
}
