package com.example.demo.dto.question;

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
public class Answer {
	@SerializedName("A")
    String A;
    
    @SerializedName("B")
    String B;
    
    @SerializedName("C")
    String C;
    
    @SerializedName("D")
    String D;
    
	@SerializedName("đáp_án_đúng")
	String correct;

	@SerializedName("giải_thích")
	String explain;
}
