package com.example.demo.modules.document.annotation.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.enums.AnnotationType;
import com.example.demo.modules.document.shared.domain.model.PdfAnnotationRect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * DTO trả về thông tin ghi chú cho Client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfAnnotationResponse {
    Long id;
    Long pdfId;
    AnnotationType type;
    String color;
    Integer page;
    String text;
    String comment;
    List<PdfAnnotationRect> rects;
    LocalDateTime createdAt;
}
