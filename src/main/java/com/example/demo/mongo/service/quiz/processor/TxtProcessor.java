package com.example.demo.mongo.service.quiz.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of document processor for Plain Text (TXT) files.
 * 
 * <p>
 * Trích xuất nội dung từ các tệp văn bản thuần túy sử dụng bảng mã
 * UTF-8 để đảm bảo hiển thị đúng các ký tự tiếng Việt.
 *
 * @since 1.0
 */
@Component
public class TxtProcessor implements IDocumentProcessor {

    private static final String MIME_TYPE = "text/plain";

    @Override
    public boolean supports(String contentType) {
        return MIME_TYPE.equals(contentType);
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
