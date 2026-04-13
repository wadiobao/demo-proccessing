package com.example.demo.modules.document.upload.application.port.output;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Output Port: Hợp đồng (Contract) giao tiếp với Storage bên ngoài.
 * Application Layer gọi thông qua Interface này, không biết đến Cloudinary/S3.
 */
public interface FileStoragePort {
    
    /**
     * Upload file và trả về Map chứa public_id và secure_url.
     */
    Map<String, String> uploadFile(MultipartFile file) throws IOException;
    
    /**
     * Xóa hàng loạt file theo danh sách public_id từ Cloud Storage.
     */
    void deleteFiles(List<String> fileIds) throws Exception;
}
