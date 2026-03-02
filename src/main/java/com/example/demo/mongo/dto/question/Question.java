package com.example.demo.mongo.dto.question;

import com.google.gson.annotations.SerializedName;

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
public class Question {
	@SerializedName("câu_hỏi_số")
	int id;

	@SerializedName("nội_dung")
	String question;

	@SerializedName("đáp_án")
	Answer answer;

	@SerializedName("lệnh_vẽ_hình")
	String imgPrompt;

	@SerializedName("tham_chiếu_mục_lục")
	String reference;

	@SerializedName("bloom_level")
	String bloomLevel;

	@SerializedName("difficulty")
	float difficulty;

	String imgUrl;

	String imgPublicId;

	@SerializedName("bank_id")
	String bankId;
}
