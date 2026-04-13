package com.example.demo.modules.document.shared.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đối tượng đại diện cho một tọa độ bôi đen trên PDF.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfAnnotationRect {
    /** Độ lệch từ trên xuống (tính theo %) */
    @Column(name = "rect_top")
    private Double top;
    /** Độ lệch từ trái sang (tính theo %) */
    @Column(name = "rect_left")
    private Double left;
    /** Chiều rộng của ô bôi đen (tính theo %) */
    @Column(name = "rect_width")
    private Double width;
    /** Chiều cao của ô bôi đen (tính theo %) */
    @Column(name = "rect_height")
    private Double height;
}
