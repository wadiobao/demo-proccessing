package com.example.demo.sql.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.repository.PdfFileRepository;
import com.example.demo.utils.CloudinaryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles incoming Cloudinary webhook events for file lifecycle management.
 *
 * <p>
 * Xác thực chữ ký HMAC-SHA1 từ Cloudinary để đảm bảo mọi yêu cầu đều
 * đến từ nguồn tin cậy. Phản hồi 401 nếu chữ ký không hợp lệ.
 *
 * @since 1.1
 */
@RestController
@RequestMapping("/api/webhook/cloudinary")
@RequiredArgsConstructor
@Slf4j
public class CloudinaryWebhookController {

	private final PdfFileRepository pdfFileRepository;

	// injected from properties to avoid hardcoding the secret in logic
	// / inject từ properties để tránh nhúng cứng secret vào code
	@Value("${cloudinary.api.secret}")
	private String cloudinaryApiSecret;

	@PostMapping
	public ResponseEntity<Void> handleCloudinaryWebhook(
			@RequestBody String payloadBody,
			@RequestHeader(value = "X-Cld-Signature", required = false) String signature,
			@RequestHeader(value = "X-Cld-Timestamp", required = false) String timestamp) {

		// reject requests without authentication headers — unauthenticated callers must
		// be denied early
		// / từ chối yêu cầu không có header xác thực — không được phép xử lý tiếp
		if (signature == null || timestamp == null) {
			log.warn("Cloudinary webhook rejected: missing signature or timestamp headers.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// verify the payload signature against API_SECRET to prevent spoofed
		// notifications
		// / xác minh chữ ký payload với API_SECRET để chống giả mạo thông báo
		if (!CloudinaryUtils.verifyNotificationSignature(payloadBody, timestamp, signature, cloudinaryApiSecret)) {
			log.warn("Cloudinary webhook rejected: invalid HMAC signature.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// check for timestamp replay window to prevent stale notification reuse
		// / kiểm tra cửa sổ thời gian timestamp để ngăn chặn việc sử dụng lại thông báo
		// cũ
		try {
			long requestTime = Long.parseLong(timestamp);
			long now = Instant.now().getEpochSecond();
			if (Math.abs(now - requestTime) > 300) { // 5-minute threshold
				log.warn("Cloudinary webhook rejected: timestamp {} is outside the valid ±300s window (now: {}).",
						requestTime, now);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
		} catch (NumberFormatException e) {
			log.warn("Cloudinary webhook rejected: invalid timestamp format {}.", timestamp);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		log.info("Cloudinary webhook received and validated.");

		// Parse JSON body manually — controller receives raw String for signature
		// validation
		// / Phân tích cú pháp JSON thủ công — controller nhận String thô để xác thực
		// chữ ký
		Map<String, Object> payload = parsePayload(payloadBody);
		if (payload == null) {
			return ResponseEntity.badRequest().build();
		}

		String assetFolder = (String) payload.get("asset_folder");
		if (!"pdf_store".equals(assetFolder)) {
			return ResponseEntity.ok().build();
		}

		String eventType = (String) payload.get("notification_type");

		if ("upload".equals(eventType)) {
			handleUpload(payload);
		} else if ("delete".equals(eventType)) {
			handleDelete(payload);
		} else if ("resource_display_name_changed".equals(eventType)) {
			handleRename(payload);
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * Persists a new file record when Cloudinary signals a successful upload.
	 *
	 * @param payload parsed webhook payload / payload đã được phân tích
	 */
	private void handleUpload(Map<String, Object> payload) {
		String publicId = (String) payload.get("public_id");
		String url = (String) payload.get("secure_url");
		String title = publicId.substring(publicId.lastIndexOf("/") + 1);

		PdfFile newFile = PdfFile.builder().title(title).cloudinaryId(publicId).pdfUrl(url).build();
		pdfFileRepository.save(newFile);
		log.info("New file added to database: {}", title);
	}

	/**
	 * Removes a file record when Cloudinary signals a deletion event.
	 * Uses indexed findByCloudinaryId to avoid a full table scan.
	 *
	 * @param payload parsed webhook payload / payload đã được phân tích
	 */
	private void handleDelete(Map<String, Object> payload) {
		String publicId = (String) payload.get("public_id");
		// use findByCloudinaryId (indexed) instead of findAll().stream().filter() which
		// caused O(n) full scan
		// / dùng findByCloudinaryId (có index) thay vì findAll().stream().filter() gây
		// quét toàn bảng O(n)
		Optional<PdfFile> existingFile = pdfFileRepository.findByCloudinaryId(publicId);
		existingFile.ifPresent(pdfFile -> {
			pdfFileRepository.delete(pdfFile);
			log.info("File removed from database: {}", publicId);
		});
	}

	/**
	 * Updates a file's display name when Cloudinary signals a rename event.
	 *
	 * @param payload parsed webhook payload / payload đã được phân tích
	 */
	@SuppressWarnings("unchecked")
	private void handleRename(Map<String, Object> payload) {
		Map<String, Object> resources = (Map<String, Object>) payload.get("resources");
		if (resources == null || resources.isEmpty())
			return;

		String firstKey = resources.keySet().iterator().next();
		Map<String, Object> resourceData = (Map<String, Object>) resources.get(firstKey);
		if (resourceData == null)
			return;

		String publicId = (String) resourceData.get("public_id");
		String newDisplayName = (String) resourceData.get("new_display_name");
		log.info("File {} renamed to: {}", publicId, newDisplayName);

		pdfFileRepository.findByCloudinaryId(publicId).ifPresent(pdfFile -> {
			pdfFile.setTitle(newDisplayName);
			pdfFileRepository.save(pdfFile);
			log.info("Display name updated in database.");
		});
	}

	/**
	 * Deserializes the raw JSON string body to a Map using Jackson.
	 * Required because raw String body is accepted for HMAC validation.
	 *
	 * @param body raw JSON body / chuỗi JSON thô
	 * @return parsed Map or null if parsing fails / Map đã phân tích hoặc null
	 */
	private Map<String, Object> parsePayload(String body) {
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			return mapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			log.error("Failed to parse webhook payload: {}", e.getMessage());
			return null;
		}
	}
}
