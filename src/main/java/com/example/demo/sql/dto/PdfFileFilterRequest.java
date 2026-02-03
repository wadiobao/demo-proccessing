package com.example.demo.sql.dto;

import com.example.demo.enums.Major;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfFileFilterRequest {
	Major major;
	int size;
	int numPage;
	
}
