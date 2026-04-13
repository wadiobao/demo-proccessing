package com.example.demo.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for media management via Cloudinary cloud service.
 * 
 * <p>
 * Hỗ trợ tải lên, xóa và quản lý các tài nguyên hình ảnh
 * sử dụng Cloudinary API phục vụ cho avatar và ảnh câu hỏi.
 *
 * @since 1.0
 */
@Component
@Slf4j
public class CloudinaryUtils {

	private String cloudName;
	private String apiKey;
	private String apiSecret;
	private Map<String, Object> params;
	Cloudinary cloudinary;

	/**
	 * Initializes Cloudinary configuration with provided credentials.
	 * 
	 * @param cloudName cloud service identifier / tên định danh đám mây
	 * @param apiKey    service access key / mã truy cập API
	 * @param apiSecret service secure secret / mã bí mật API
	 */
	public CloudinaryUtils(String cloudName, String apiKey, String apiSecret) {
		this.cloudName = cloudName;
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		// aggregate credentials into a single configuration block for the Cloudinary
		// client
		// / tập hợp các thông tin xác thực vào một khối cấu hình duy nhất cho
		// Cloudinary client
		Map<String, String> config = new HashMap<String, String>();
		config.put("cloud_name", cloudName);
		config.put("api_key", apiKey);
		config.put("api_secret", apiSecret);
		this.cloudinary = new Cloudinary(config);
		params = new HashMap<>();
		params.put("asset_folder", "question_images");
	}

	/**
	 * Uploads an image file to the dedicated question folder.
	 * 
	 * @param fileName path or identifier of the file to upload / đường dẫn file cần
	 *                 tải lên
	 * @return array with [public_id, secure_url] / mảng chứa thông tin định danh và
	 *         URL ảnh
	 * @throws IOException in case of network or file errors / lỗi đọc file hoặc kết
	 *                     nối
	 */
	public String[] upload(String fileName) throws IOException {
		Map<String, Object> uploadResult = cloudinary.uploader().upload(fileName, params);
		String secureUrl = (String) uploadResult.get("secure_url");
		String publicId = (String) uploadResult.get("public_id");
		return new String[] { publicId, secureUrl };
	}

	/**
	 * Removes resources from Cloudinary by their public identifiers.
	 * 
	 * @param publicId list of resource IDs to delete / danh sách ID tài nguyên cần
	 *                 xóa
	 * @throws Exception for API interaction failures / lỗi tương tác với Cloudinary
	 *                   API
	 */
	public void delete(List<String> publicId) throws Exception {
		cloudinary.api().deleteResources(publicId, params);
	}

	/**
	 * Handles avatar image uploads with specific folder assignment.
	 * 
	 * @param file multipart file containing the image / file ảnh nhận từ request
	 * @return secure URL of the uploaded avatar / URL bảo mật của ảnh đại diện
	 * @throws IOException if byte processing fails / lỗi xử lý dữ liệu nhị phân của
	 *                     ảnh
	 */
	public String uploadAvatar(MultipartFile file) throws IOException {
		Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
				"folder", "avatars",
				"resource_type", "image"));
		return (String) uploadResult.get("secure_url");
	}

	/**
	 * Uploads a PDF file to the dedicated PDF store folder.
	 *
	 * @param file multipart file containing the PDF / file PDF nhận từ request
	 * @return Map containing public_id and secure_url / Map chứa thông tin định danh
	 *         và URL
	 * @throws IOException if byte processing fails / lỗi xử lý dữ liệu nhị phân của
	 *                     file
	 */
	public Map<String, String> uploadPdf(MultipartFile file) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
				"folder", "pdf_store",
				"resource_type", "auto"));

		Map<String, String> result = new HashMap<>();
		result.put("public_id", (String) uploadResult.get("public_id"));
		result.put("secure_url", (String) uploadResult.get("secure_url"));
		return result;
	}

	/**
	 * Verifies the authenticity of a Cloudinary webhook notification by recomputing
	 * the HMAC-SHA1 signature and comparing it against the provided header value.
	 * Rejects spoofed requests that lack a valid signature.
	 *
	 * @param payloadBody raw request body string / nội dung body yêu cầu thô
	 * @param timestamp   X-Cld-Timestamp header value / giá trị header timestamp
	 * @param signature   X-Cld-Signature header value / giá trị chữ ký từ header
	 * @param apiSecret   Cloudinary API secret / mã bí mật API của Cloudinary
	 * @return true if signature is valid, false otherwise / true nếu chữ ký hợp lệ
	 */
	public static boolean verifyNotificationSignature(
			String payloadBody, String timestamp, String signature, String apiSecret) {
		try {
			// Cloudinary spec: sign(payload_body + timestamp) with API_SECRET using
			// HMAC-SHA1
			// / Spec Cloudinary: ký (payload_body + timestamp) với API_SECRET bằng
			// HMAC-SHA1
			String message = payloadBody + timestamp;
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
			byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString().equals(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			log.error("Webhook signature verification failed: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Decodes a Base64 image, saves it locally, and uploads to Cloudinary.
	 * 
	 * @param base64String raw image data
	 * @param fileName     temporary filename
	 * @return Cloudinary metadata [public_id, secure_url]
	 */
	public String[] saveImageFromBase64(String base64String, String fileName) {
		try {
			byte[] imageBytes = Base64.getDecoder().decode(base64String);
			Files.write(Paths.get(fileName), imageBytes);
			String[] imgAtt = this.upload(fileName);
			if (Files.deleteIfExists(Paths.get(fileName))) {
				log.info("Temporary image file deleted from local storage.");
			}
			return imgAtt;
		} catch (IOException e) {
			log.error("Error saving image file: {}", e.getMessage());
		}
		return null;
	}
}
