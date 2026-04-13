package com.example.demo.modules.document.upload.infrastructure.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.utils.CloudinaryUtils;
import com.example.demo.modules.document.upload.application.port.output.FileStoragePort;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Adapter thực thi giao tiếp Storage cho Cloudinary.
 * Nằm ở Infrastructure Layer để cách ly thư viện bên ngoài khỏi UseCase.
 */
@Component
@RequiredArgsConstructor
public class CloudinaryFileStorageAdapter implements FileStoragePort {

    private final CloudinaryUtils cloudinaryUtils;

    @Override
    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        return cloudinaryUtils.uploadPdf(file);
    }

    @Override
    public void deleteFiles(List<String> fileIds) throws Exception {
        cloudinaryUtils.delete(fileIds);
    }
}
