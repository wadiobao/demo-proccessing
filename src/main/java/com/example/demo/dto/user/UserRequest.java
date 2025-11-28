package com.example.demo.dto.user;

import java.util.Date;

import org.springframework.data.redis.core.RedisHash;

import com.example.demo.validation.DateConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class UserRequest {
	@Size(min = 5, message = "INVALID_USERNAME")
	private String userName;
	@Size(min = 8, message = "INVALID_PASSWORD")
	private String password;
	private String email;
	
	@DateConstraint(min = 16,message = "INVALID_DATE")
	@JsonFormat(pattern = "dd/MM/yyyy")
	private Date date;	
	
}
