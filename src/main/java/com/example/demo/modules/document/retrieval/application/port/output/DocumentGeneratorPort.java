package com.example.demo.modules.document.retrieval.application.port.output;

import java.util.Map;

/**
 * Port for generating documents in various formats.
 * 
 * <p>
 * Định nghĩa hợp đồng cho việc tạo các tệp tin (Word, PDF) 
 * từ dữ liệu và mẫu (Template) có sẵn.
 */
public interface DocumentGeneratorPort {

    /**
     * Generates a document and returns it as a Base64 encoded string.
     * 
     * @param templatePath path to the template file
     * @param data variables to inject into the template
     * @param targetFormat format to generate (e.g., "DOCX", "PDF")
     * @return Base64 encoded document string
     * @throws Exception if generation fails
     */
    String generateBase64(String templatePath, Map<String, Object> data, String targetFormat) throws Exception;

    /**
     * Checks if this generator supports the target format.
     * 
     * @param format target format
     * @return true if supported
     */
    boolean supports(String format);
}
