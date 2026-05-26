package com.example.demo.modules.identity.infrastructure.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    // Tiêm thẳng GOOGLE_CLIENT_ID từ file cấu hình vào đây để tự động validate 'aud'
    public GoogleTokenVerifier(@Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload(); // Trả về tập dữ liệu gồm email, name, picture, sub
            } else {
                throw new IllegalArgumentException("Google ID Token không hợp lệ hoặc đã hết hạn.");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Lỗi hệ thống khi xác thực Google Token", e);
        }
    }
}
