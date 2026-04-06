package com.example.demo.sql.entity;

import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.enums.AnnotationType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * Ghi chú hoặc highlight trên tập tin PDF.
 * Được phân tách bởi từng User để đảm bảo tính riêng tư.
 */
@Entity
@Table(name = "pdf_annotations", indexes = {
    @Index(name = "idx_pdf_user", columnList = "pdf_id, user_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfAnnotation extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_id", nullable = false)
    PdfFile pdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    AnnotationType type;

    /** Mã màu highlight (e.g., #FFFF00) */
    String color;
    
    /** Trang của file PDF */
    Integer page;

    /** Văn bản được bôi đen từ PDF */
    @Column(columnDefinition = "TEXT")
    String text;

    /** Nội dung bình luận của người dùng */
    @Column(columnDefinition = "TEXT")
    String comment;

    /** Tập hợp các ô tọa độ bôi đen (Value Object Pattern) */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pdf_annotation_rects", joinColumns = @JoinColumn(name = "annotation_id"))
    @BatchSize(size = 30)
    List<PdfAnnotationRect> rects;
}
