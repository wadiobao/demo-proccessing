package com.example.demo.dto.form;

import java.util.Date;
import java.util.Set;

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
public class FormResponse {
	String formId;
	String tacGia;
	String tieuDe;
	Set<String> tags;
	Date ngayDang;
	String noiDung;
	String topic;
}
