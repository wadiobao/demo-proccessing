package com.example.demo.modules.document.upload.domain.service;

import org.springframework.stereotype.Service;

import com.example.demo.modules.document.upload.domain.valueobject.FileFormat;
import com.example.demo.modules.document.upload.domain.valueobject.FileSize;

/**
 * Domain Service tập trung kiểm duyệt mọi Business Rule liên quan đến chính sách đưa file lên Cloud.
 * Các rule phức tạp liên đới nhiều model có thể được tiêm vào đây.
 */
@Service
public class UploadPolicyDomainService {

    /**
     * Xác thực xem định dạng và kích thước có tuân thủ mọi luật lệ hay không.
     * Mặc dù FileFormat và FileSize tự thân validation khi khởi tạo rồi, 
     * nhưng có thể kết hợp thêm rule (VD: FileFormat = abc thì MaxFileSize khác).
     */
    public void enforceUploadPolicy(FileSize size, FileFormat format) {
        // Business Rule mẫu: Nếu là PDF thì không được bé hơn 1KB
        if (format.getMimeType().equals("application/pdf") && size.getBytes() < 1024) {
            throw new IllegalArgumentException("Hệ thống từ chối file PDF rác (kích thước quá nhỏ < 1KB).");
        }
        
        // ... Các quy tắc về quyền upload dựa vào gói nâng cấp của User có thể vào đây nếu cần thiết
    }
}
