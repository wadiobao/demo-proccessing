package com.example.demo.modules.document.upload.application.validator;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.document.upload.application.command.UploadDocumentCommand;
import com.example.demo.modules.document.upload.domain.service.PdfSafetyDomainService;
import com.example.demo.modules.document.upload.domain.service.UploadPolicyDomainService;
import com.example.demo.modules.document.upload.domain.valueobject.FileFormat;
import com.example.demo.modules.document.upload.domain.valueobject.FileSize;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UploadDocumentValidator {

    private final UploadPolicyDomainService uploadPolicyDomainService;
    private final PdfSafetyDomainService pdfSafetyDomainService;

    /**
     * Thực hiện chuỗi validation đầy đủ theo thứ tự từ nông đến sâu:
     * <ol>
     *   <li>Null / empty check</li>
     *   <li>FileSize và FileFormat Value Objects (kích thước, MIME whitelist)</li>
     *   <li>Upload policy (PDF > 1KB, rate-limit nếu cần)</li>
     *   <li>Kiểm tra an toàn nội dung PDF (magic bytes, JS, page bomb, filename)</li>
     * </ol>
     *
     * @param command lệnh upload chứa file và metadata
     */
    public void validate(UploadDocumentCommand command) {
        if (command.getFile() == null || command.getFile().isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để trống.");
        }

        // Khởi tạo Value Objects (tự validation kích thước & định dạng)
        FileSize fileSize = new FileSize(command.getFile().getSize());
        FileFormat fileFormat = new FileFormat(command.getFile().getContentType());

        // Kiểm tra luật liên ngành: PDF > 1KB, v.v.
        uploadPolicyDomainService.enforceUploadPolicy(fileSize, fileFormat);

        if (command.getRequest() == null) {
            throw new IllegalArgumentException("Thông tin (metadata) của file không được để trống.");
        }

        // Kiểm tra nội dung thực tế của file để phát hiện mã độc
        try {
            pdfSafetyDomainService.verify(
                    command.getFile().getInputStream(),
                    command.getFile().getOriginalFilename()
            );
        } catch (IOException e) {
            // Không thể đọc stream — treat as untrusted input
            throw new HandleException(ErrorCode.MALICIOUS_FILE);
        }
    }
}
