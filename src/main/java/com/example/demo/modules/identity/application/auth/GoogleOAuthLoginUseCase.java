package com.example.demo.modules.identity.application.auth;

import java.util.concurrent.CompletableFuture;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.modules.identity.api.dto.GoogleLoginRequest;
import com.example.demo.modules.identity.api.dto.LoginResult;
import com.example.demo.modules.identity.domain.model.AuthProvider;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.port.MailPort;
import com.example.demo.modules.identity.domain.repository.IRoleRepository;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.identity.infrastructure.security.GoogleTokenVerifier;
import com.example.demo.modules.identity.infrastructure.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthLoginUseCase {
    private final IRoleRepository roleRepository;
    private final ITierRepository tierRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final IUserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final MailPort mailPort;
    
    @Transactional
    public LoginResult execute(GoogleLoginRequest request) {
        // 1. Xác thực token với Google
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());

        // BẢO MẬT: Đảm bảo email đã được xác minh chính chủ từ Google
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new HandleException(ErrorCode.UNAUTHORIZED);
        }

        String email = payload.getEmail();
        String googleUserId = payload.getSubject(); // 'sub' claim (Mã định danh duy nhất của user bên Google)
        String name = (String) payload.get("name");

        // 2. Tìm kiếm và liên kết user trong DB hệ thống
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    // AN TOÀN: Sử dụng pattern matching instanceof để tránh ClassCastException
                    if (existingUser instanceof NormalUser normalUser) {
                        // Nếu tài khoản tồn tại dạng LOCAL thì liên kết với Google
                        if (normalUser.getProvider() == AuthProvider.LOCAL) {
                            normalUser.setProvider(AuthProvider.GOOGLE);
                            normalUser.setProviderId(googleUserId);
                            return userRepository.save(normalUser);
                        }
                    } else {
                        throw new HandleException(ErrorCode.UNAUTHORIZED);
                    }
                    return existingUser;
                })
                // Nếu chưa có email này trong hệ thống -> Tạo tài khoản tự động mới
                .orElseGet(() -> {
                    Role role = roleRepository.findById("USER")
                            .orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

                    Tier tier = tierRepository.findById("MEMBER")
                            .orElseThrow(() -> new HandleException(ErrorCode.UNCATEGORIZED_EXCEPTION));

                    // Tạo mật khẩu ngẫu nhiên và mã hóa bằng BCrypt để đăng nhập cục bộ
                    String rawPassword = generateRandomPassword();
                    String encodedPassword = passwordEncoder.encode(rawPassword);

                    NormalUser newUser = NormalUser.builder()
                    		.avatarUrl((String)payload.get("picture"))
                            .userName(name)
                            .password(encodedPassword)
                            .email(email)
                            .role(role)
                            .reputationScore(0)
                            .currentTier(tier)
                            .provider(AuthProvider.GOOGLE)
                            .providerId(googleUserId)
                            .build();
                    
                    User savedUser = userRepository.save(newUser);

                    // Gửi email chứa thông tin tài khoản và mật khẩu được tạo ngẫu nhiên
                    CompletableFuture.runAsync(() -> {
                    	try {
                            String subject = "Wirazrd - Thông tin tài khoản liên kết Google mới";
                            String text = String.format(
                                "Chào %s,\n\nBạn vừa đăng nhập thành công bằng Google trên hệ thống của chúng tôi.\n" +
                                "Tài khoản của bạn đã được khởi tạo tự động.\n\n" +
                                "Dưới đây là thông tin mật khẩu đăng nhập cục bộ của bạn để có thể đăng nhập bằng hình thức thông thường:\n" +
                                "- Tài khoản/Email: %s\n" +
                                "- Mật khẩu: %s\n\n" +
                                "Vui lòng đổi mật khẩu sau khi đăng nhập để đảm bảo an toàn tối đa cho tài khoản của bạn.\n\nTrân trọng,\nĐội ngũ vận hành.",
                                name, email, rawPassword
                            );
                            mailPort.sendSimpleMail(email, subject, text);
                        } catch (Exception e) {
                            log.error("Không thể gửi email thông báo mật khẩu cho user: {}", email, e);
                        }

                    });
                    
                    return savedUser;
                });
        
        String accessToken = jwtUtils.generateToken(user, false);
        String refreshToken = jwtUtils.generateToken(user, true);

        return LoginResult.builder()
                .accessTokenCookie(jwtUtils.generateAccessCookie(accessToken))
                .refreshTokenCookie(jwtUtils.generateRefreshCookie(refreshToken))
                .authenticated(true)
                .build();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
