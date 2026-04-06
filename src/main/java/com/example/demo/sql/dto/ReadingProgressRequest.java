package com.example.demo.sql.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO để nhận dữ liệu tiến trình đọc từ Frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingProgressRequest {
    Long pdfId;
    Double scrollPercent;
    Integer lastPage;
}
