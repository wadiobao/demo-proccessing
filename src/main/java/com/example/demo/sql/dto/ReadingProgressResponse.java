package com.example.demo.sql.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO trả về thông tin tiến trình đọc cho Frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingProgressResponse {
    Long pdfId;
    Double scrollPercent;
    Integer lastPage;
}
