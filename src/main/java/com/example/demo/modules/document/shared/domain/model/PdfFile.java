package com.example.demo.modules.document.shared.domain.model;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.enums.FileType;
import com.example.demo.sql.entity.Major;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "pdf_files")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfFile extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String title;
	String pdfUrl;
	String cloudinaryId;

	@jakarta.persistence.ManyToOne
	@jakarta.persistence.JoinColumn(name = "major_id")
	Major major;

	FileType fileType;
	String author;
}
