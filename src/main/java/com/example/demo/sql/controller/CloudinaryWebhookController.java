package com.example.demo.sql.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.repository.PdfFileRepository;

@RestController
@RequestMapping("/api/webhook/cloudinary")
public class CloudinaryWebhookController {
	private final PdfFileRepository pdfFileRepository;

	public CloudinaryWebhookController(PdfFileRepository pdfFileRepository) {
		this.pdfFileRepository = pdfFileRepository;
	}

	@PostMapping
	public void handleCloudinaryWebhook(@RequestBody Map<String, Object> payload) {
		System.out.println("📩 Received Webhook: " + payload);

		String assetFolder = (String) payload.get("asset_folder");
		if ("pdf_store".equals(assetFolder)) {
			// Lấy event từ Cloudinary
			String eventType = (String) payload.get("notification_type");

			// Nếu là file mới được upload
			if ("upload".equals(eventType)) {
				String publicId = (String) payload.get("public_id");

				String url = (String) payload.get("secure_url");
				String title = publicId.substring(publicId.lastIndexOf("/") + 1);

				PdfFile newFile = PdfFile.builder().title(title).cloudinaryId(publicId).pdfUrl(url).build();
				pdfFileRepository.save(newFile);
				System.out.println("✅ Đã thêm file mới vào database: " + title);
			}

			// Nếu là file bị xóa
			if ("delete".equals(eventType)) {
				String publicId = (String) payload.get("public_id");

				Optional<PdfFile> existingFile = pdfFileRepository.findAll().stream()
						.filter(file -> file.getCloudinaryId().equals(publicId)).findFirst();

				existingFile.ifPresent(pdfFile -> {
					pdfFileRepository.delete(pdfFile);
					System.out.println("❌ Đã xóa file khỏi database: " + publicId);
				});
			}

			if ("resource_display_name_changed".equals(eventType)) {
				Map<String, Object> resources = (Map<String, Object>) payload.get("resources");

				if (resources != null && !resources.isEmpty()) {
					// Lấy key đầu tiên trong "resources"
					String firstKey = resources.keySet().iterator().next();
					Map<String, Object> resourceData = (Map<String, Object>) resources.get(firstKey);

					if (resourceData != null) {
						String publicId = (String) resourceData.get("public_id");
						String newDisplayName = (String) resourceData.get("new_display_name");

						System.out.println("🔄 File có ID: " + publicId + " đã đổi tên thành: " + newDisplayName);

						Optional<PdfFile> existingFile = pdfFileRepository.findByCloudinaryId(publicId);
						existingFile.ifPresent(pdfFile -> {
							pdfFile.setTitle(newDisplayName);
							pdfFileRepository.save(pdfFile);
							System.out.println("✅ Đã cập nhật display name trong database.");
						});
					}
				}
			}
		}
	}
}
