package com.example.demo.sql.dto.user;

import java.util.Date;
import java.util.Set;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private String userName;
	private String email;
	private Date date;
	private String avatarUrl;
	private Set<String> roles;
	private int reputationScore;
	private String currentTier;
}
