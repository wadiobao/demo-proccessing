package com.example.demo.sql.dto;

import java.util.List;
import com.example.demo.enums.AnnotationType;
import com.example.demo.sql.entity.PdfAnnotationRect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO để lưu trữ ghi chú mới từ Client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfAnnotationRequest {
    Long pdfId;
    AnnotationType type;
    String color;
    Integer page;
    String text;
    String comment;
    List<PdfAnnotationRect> rects;
}
