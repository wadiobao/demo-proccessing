package com.example.demo.modules.community.forum.api.dto;

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
public class CommentResponse {
	Long id;
	String tacGia;
	String noiDung;
	Date ngayComment;
	Boolean hasChanged;
	Boolean isAuthor;
}
