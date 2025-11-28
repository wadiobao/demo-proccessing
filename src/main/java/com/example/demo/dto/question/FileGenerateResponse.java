package com.example.demo.dto.question;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FileGenerateResponse {
	List<Question> questions;
	String wordBase64;
	String pdfBase64;
	@JsonIgnore
	String contentPdf;
}
