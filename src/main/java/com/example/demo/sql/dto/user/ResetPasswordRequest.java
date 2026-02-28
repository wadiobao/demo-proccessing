package com.example.demo.sql.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "EMAIL_IS_REQUIRED")
    private String email;

    @NotBlank(message = "OTP_IS_REQUIRED")
    private String otp;

    @Size(min = 8, message = "INVALID_PASSWORD")
    private String newPassword;
}
