package com.example.demo.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

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
}
