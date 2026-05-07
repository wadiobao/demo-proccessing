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
public class UserRegistrationRequest {
    private String userName;
    private String password;
    private String email;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date date;
}
