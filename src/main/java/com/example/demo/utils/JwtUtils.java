package com.example.demo.utils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.entity.NormalUser;
import com.example.demo.sql.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.NonFinal;

/**
 * Core utility for JSON Web Token (JWT) lifecycle management.
 * 
 * <p>
 * Chịu trách nhiệm mã hóa, ký và xác thực token bảo mật,
 * hỗ trợ cơ chế Stateless Authentication cho toàn bộ hệ thống.
 *
 * @since 1.0
 */
@Component
public class JwtUtils {

	@Autowired
	InvalidatedTokenRepository invalidatedTokenRepository;

	@NonFinal
	@Value("${demo.secret.key}")
	String SIGN_KEY;

	@NonFinal
	@Value("${demo.time.token.refresh}")
	int REFRESH_TiME;

	@NonFinal
	@Value("${demo.time.token.access}")
	int ACCESS_TiME;

	/**
	 * Generates a signed JWT with specific claims for a user.
	 * 
	 * @param user      candidate entity for identity / thực thể người dùng cần cấp
	 *                  token
	 * @param isRefresh true for long-lived refresh token / true nếu là token làm
	 *                  mới
	 * @return serialized signed JWT string / chuỗi JWT đã được ký và tuần tự hóa
	 */
	public String generateToken(User user, boolean isRefresh) {
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
		JWTClaimsSet claimsSet;
		if (isRefresh) {
			claimsSet = new JWTClaimsSet.Builder().jwtID(UUID.randomUUID().toString()).subject(user.getUserName())
					.issueTime(new Date())
					.expirationTime(new Date(Instant.now().plus(REFRESH_TiME, ChronoUnit.DAYS).toEpochMilli())).build();
		} else {
			claimsSet = new JWTClaimsSet.Builder().jwtID(UUID.randomUUID().toString()).subject(user.getUserName())
					.issuer("freequizai.com").issueTime(new Date())
					.expirationTime(new Date(Instant.now().plus(ACCESS_TiME, ChronoUnit.DAYS).toEpochMilli()))
					.claim("scope", buildScope(user)).build();
		}

		Payload payload = new Payload(claimsSet.toJSONObject());

		JWSObject jwsObject = new JWSObject(header, payload);

		try {
			jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));

		} catch (KeyLengthException e) {
			e.printStackTrace();
		} catch (JOSEException e) {
			e.printStackTrace();
		}
		return jwsObject.serialize();

	}

	/**
	 * Aggregates user roles into a space-separated scope string.
	 * 
	 * @param user source entity / người dùng nguồn
	 * @return space delimited role string / chuỗi chứa các quyền cách nhau bởi dấu
	 *         cách
	 */
	public String buildScope(User user) {
		StringJoiner joiner = new StringJoiner(" ");
		if (!CollectionUtils.isEmpty(user.getRoles())) {
			user.getRoles().forEach(r -> {
				joiner.add("ROLE_" + r.getName());
				if (!org.springframework.util.CollectionUtils.isEmpty(r.getPermissions())) {
					r.getPermissions().forEach(p -> joiner.add(p.getName()));
				}
			});
		}

		if (user instanceof NormalUser normalUser && normalUser.getCurrentTier() != null) {
			joiner.add("TIER_" + normalUser.getCurrentTier().getId());
			if (!org.springframework.util.CollectionUtils.isEmpty(normalUser.getCurrentTier().getPermissions())) {
				normalUser.getCurrentTier().getPermissions().forEach(p -> joiner.add(p.getName()));
			}
		}

		return joiner.toString();
	}

	/**
	 * Validates token signature and expiration.
	 * 
	 * @param token raw JWT string / chuỗi token thô
	 * @return fully parsed SignedJWT if valid / đối tượng SignedJWT nếu hợp lệ
	 * @throws JOSEException  for cryptographic failures / lỗi giải mật mã
	 * @throws ParseException for malformed tokens / lỗi định dạng chuỗi
	 */
	public SignedJWT verifyToken(String token) throws JOSEException, ParseException {

		try {
			JWSVerifier jwsVerifier = new MACVerifier(SIGN_KEY.getBytes());

			SignedJWT jwtSignedJWT = SignedJWT.parse(token);

			Date expityTime = jwtSignedJWT.getJWTClaimsSet().getExpirationTime();

			boolean verified = jwtSignedJWT.verify(jwsVerifier);

			if (!(verified && expityTime.after(new Date()))) {
				throw new HandleException(ErrorCode.UNAUTHENTICATED);
			}

			// cross-reference against blacklist to enforce immediate session revocation
			// (e.g., after logout)
			// / kiểm tra chéo với danh sách đen để cưỡng chế hủy phiên ngay lập tức (ví dụ:
			// sau khi đăng xuất)
			if (invalidatedTokenRepository.existsById(jwtSignedJWT.getJWTClaimsSet().getJWTID())) {
				throw new HandleException(ErrorCode.UNAUTHENTICATED);
			}

			return jwtSignedJWT;

		} catch (Exception e) {
			throw new HandleException(ErrorCode.UNAUTHENTICATED);
		}
	}

	/**
	 * Constructs a secure cookie for access token transport.
	 * 
	 * @param token source secret / mã bí mật nguồn
	 * @return HTTP-only ResponseCookie / cookie bảo mật chỉ đọc bởi server
	 */
	public ResponseCookie generateAccessCookie(String token) {
		// SameSite=Lax balance: allows top-level navigation while blocking cross-site
		// CSRF on subresources
		// / Chế độ SameSite=Lax: cho phép điều hướng cấp cao nhất trong khi chặn CSRF
		// chéo trang trên các tài nguyên con
		return ResponseCookie.from("access-token", token).httpOnly(true).path("/").maxAge(ACCESS_TiME)
				.sameSite("Lax").build();
	}

	public ResponseCookie generateRefreshCookie(String token) {
		return ResponseCookie.from("refresh-token", token).httpOnly(true).path("/").maxAge(REFRESH_TiME)
				.sameSite("Lax").build();
	}

	/**
	 * Resets authentication state by clearing secure cookies.
	 * 
	 * @param response current servlet response / đối tượng phản hồi hiện tại
	 */
	public void clearToken(HttpServletResponse response) {
		ResponseCookie accessTokenCookie = ResponseCookie.from("access-token", "").httpOnly(true).path("/").maxAge(0)
				.build();
		response.addHeader("Set-Cookie", accessTokenCookie.toString());

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh-token", "").httpOnly(true).path("/").maxAge(0)
				.build();
		response.addHeader("Set-Cookie", refreshTokenCookie.toString());
	}

}
