package com.example.demo.modules.identity.api.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String userName;
    private String email;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date date;
    private String currentTier;
    private String avatarUrl;
    private int reputationScore;
}
