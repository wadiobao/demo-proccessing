package com.example.demo.modules.document.upload.application.validator;

import org.springframework.stereotype.Component;

import com.example.demo.modules.document.upload.application.command.UploadPdfCommand;
import com.example.demo.modules.document.upload.domain.service.UploadPolicyDomainService;
import com.example.demo.modules.document.upload.domain.valueobject.FileFormat;
import com.example.demo.modules.document.upload.domain.valueobject.FileSize;

import lombok.RequiredArgsConstructor;

// TODO FIXME: Đổi tên thành UploadDocumentValidator
@Component
@RequiredArgsConstructor
public class UploadPdfValidator {

    private final UploadPolicyDomainService uploadPolicyDomainService;

    public void validate(UploadPdfCommand command) {
        if (command.getFile() == null || command.getFile().isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để trống.");
        }
        
        // Khởi tạo Value Objects (Chúng sẽ tự Validation kích thước & định dạng)
        FileSize fileSize = new FileSize(command.getFile().getSize());
        FileFormat fileFormat = new FileFormat(command.getFile().getContentType());
        
        // Gọi Domain Service để kiểm tra luật liên ngành (nếu có bổ sung như Rate limit, Min size, etc.)
        uploadPolicyDomainService.enforceUploadPolicy(fileSize, fileFormat);

        if (command.getRequest() == null) {
            throw new IllegalArgumentException("Thông tin (metadata) của file không được để trống.");
        }
    }
}
