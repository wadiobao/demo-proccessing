package com.example.demo.sql.dto;

import com.example.demo.enums.FileType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for uploading or updating PDF file metadata.
 *
 * @since 1.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfFileRequest {
    String title;
    Long majorId;
    FileType fileType;
    String author;
}
