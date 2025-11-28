# Kế hoạch chuyển đổi sang Cookie-based Authentication cho Backend

Đây là kế hoạch chi tiết để chuyển đổi từ việc lưu token trong header/request param sang sử dụng cookie. Việc này giúp tăng cường bảo mật (chống lại tấn công XSS qua `HttpOnly` cookie) và đơn giản hóa phía client vì trình duyệt sẽ tự động quản lý việc gửi token.

### Kế hoạch chuyển đổi

Chúng ta sẽ thực hiện 4 bước chính:

1.  **Sửa đổi logic phát hành Token:** Thay vì trả token về trong header của response đăng nhập/refresh, chúng ta sẽ thiết lập chúng dưới dạng `HttpOnly` cookie.
2.  **Cấu hình đọc Token từ Cookie:** Hướng dẫn Spring Security tìm và đọc access token từ cookie thay vì từ `Authorization` header.
3.  **Cập nhật logic Refresh Token:** Sửa đổi endpoint refresh để đọc refresh token từ cookie thay vì request body.
4.  **Cập nhật logic Logout:** Xóa cookie chứa token khi người dùng đăng xuất.

---

### **Bước 1: Sửa đổi việc phát hành Token tại Controller**

Chúng ta cần thay đổi `AuthenticationController` để xử lý `HttpServletResponse` và thiết lập cookie. Logic trong `AuthenticationService` sẽ được sửa đổi một chút để trả về token thay vì `ResponseEntity`.

**1.1. Sửa `AuthenticationService`:**
Thay đổi phương thức `authenticate` và `refreshToken` để trả về một đối tượng chứa token, ví dụ `AuthenticationResponse`.

*   **File:** `src/main/java/com/example/demo/service/AuthenticationService.java`
*   **Thay đổi:**
    *   Trong phương thức `authenticate`, thay vì trả về `ResponseEntity`, hãy trả về một đối tượng chứa access token và refresh token.
    *   Làm tương tự cho `refreshToken`.

**1.2. Sửa `AuthenticationController`:**
Controller sẽ gọi service, nhận token, sau đó tạo và đính kèm cookie vào response.

*   **File:** `src/main/java/com/example/demo/controller/AuthenticationController.java`
*   **Thay đổi:**
    *   Inject `HttpServletResponse` vào phương thức `authenticate` và `refreshToken`.
    *   Tạo một hàm private `createCookie` để tái sử dụng.
    *   Gọi service để lấy token, sau đó dùng `response.addCookie()` để thêm `access_token` và `refresh_token`.

**Ví dụ trong `AuthenticationController`:**

```java
// Thêm vào trong AuthenticationController

@PostMapping("/login")
public ResponseEntity<StateResponse<Object>> authenticate(@RequestBody AuthenticationUser request, HttpServletResponse response) {
    // Giả sử service trả về một đối tượng chứa 2 token
    AuthenticationResponse authData = authenticationService.authenticate(request);

    // Tạo cookie cho access token
    Cookie accessTokenCookie = createCookie("access_token", authData.getAccessToken(), 1 * 24 * 60 * 60); // 1 ngày
    response.addCookie(accessTokenCookie);

    // Tạo cookie cho refresh token
    Cookie refreshTokenCookie = createCookie("refresh_token", authData.getRefreshToken(), 30 * 24 * 60 * 60); // 30 ngày
    response.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(StateResponse.builder().result("Authentication successful").build());
}

private Cookie createCookie(String name, String value, int maxAgeInSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Đặt là true ở môi trường production (HTTPS)
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeInSeconds);
    // cookie.setSameSite("Strict"); // Rất quan trọng để chống CSRF
    return cookie;
}
```

---

### **Bước 2: Cấu hình đọc Access Token từ Cookie**

Spring Security mặc định tìm token trong `Authorization` header. Chúng ta cần tạo một `BearerTokenResolver` tùy chỉnh để nó đọc từ cookie.

**2.1. Tạo `CookieJwtBearerTokenResolver`:**

*   Tạo một class mới `CookieJwtBearerTokenResolver.java`.
*   Class này sẽ implement `BearerTokenResolver`.
*   Nó sẽ duyệt qua các cookie trong request, tìm cookie có tên `access_token` và trả về giá trị của nó.

**Ví dụ:**

```java
// File: src/main/java/com/example/demo/configguration/CookieJwtBearerTokenResolver.java
package com.example.demo.configguration;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CookieJwtBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
```

**2.2. Cập nhật `SecurityConfig`:**

*   **File:** `src/main/java/com/example/demo/configguration/SecurityConfig.java`
*   **Thay đổi:**
    *   Inject `CookieJwtBearerTokenResolver` vừa tạo.
    *   Trong `filterChain`, cấu hình `oauth2ResourceServer` để sử dụng resolver này.

**Ví dụ trong `SecurityConfig`:**

```java
// Inject resolver
@Autowired
private CookieJwtBearerTokenResolver cookieJwtBearerTokenResolver;

// Trong phương thức filterChain(HttpSecurity http)
http.oauth2ResourceServer(oauth2 -> oauth2.jwt(
        jwtconfig -> jwtconfig.decoder(customJwtDecoder).jwtAuthenticationConverter(authenticationConverter()))
        .bearerTokenResolver(cookieJwtBearerTokenResolver) // Thêm dòng này
        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
```

---

### **Bước 3: Cập nhật logic Refresh Token**

Endpoint `/refresh` cần đọc refresh token từ cookie thay vì request body.

*   **File:** `src/main/java/com/example/demo/controller/AuthenticationController.java`
*   **Thay đổi:**
    *   Sửa phương thức `refreshToken` để nhận giá trị từ cookie bằng annotation `@CookieValue`.

**Ví dụ:**

```java
@PostMapping("/refresh")
public ResponseEntity<StateResponse<Object>> refreshToken(
        @CookieValue(name = "refresh_token") String refreshToken,
        HttpServletResponse response) throws JOSEException, ParseException {

    // Gọi service, logic tương tự bước 1
    AuthenticationResponse authData = authenticationService.refreshToken(refreshToken);

    // Tạo và set cookie mới
    Cookie accessTokenCookie = createCookie("access_token", authData.getAccessToken(), 1 * 24 * 60 * 60);
    response.addCookie(accessTokenCookie);

    return ResponseEntity.ok(StateResponse.builder().result("Token refreshed").build());
}
```

---

### **Bước 4: Cập nhật logic Logout**

Khi logout, chúng ta cần xóa cookie trên trình duyệt của người dùng bằng cách gửi lại cookie với `Max-Age = 0`.

*   **File:** `src/main/java/com/example/demo/controller/AuthenticationController.java`
*   **Thay đổi:**
    *   Sửa phương thức `logout` để nhận token từ cookie và xóa cả hai cookie.

**Ví dụ:**

```java
@PostMapping("/logout")
public StateResponse<Object> logout(
        @CookieValue(name = "access_token") String accessToken,
        HttpServletResponse response) throws JOSEException, ParseException {

    authenticationService.logout(accessToken); // Service chỉ cần invalidate token

    // Xóa cookie access_token
    Cookie accessTokenCookie = createCookie("access_token", "", 0);
    response.addCookie(accessTokenCookie);

    // Xóa cookie refresh_token
    Cookie refreshTokenCookie = createCookie("refresh_token", "", 0);
    response.addCookie(refreshTokenCookie);

    return StateResponse.builder().result("Logout successful").build();
}
```

### Lưu ý quan trọng về bảo mật:

*   **`HttpOnly=true`**: Bắt buộc. Ngăn chặn JavaScript phía client đọc cookie, giảm thiểu rủi ro từ tấn công XSS.
*   **`Secure=true`**: Bắt buộc trên môi trường production. Đảm bảo cookie chỉ được gửi qua kết nối HTTPS.
*   **`SameSite=Strict` hoặc `Lax`**: Rất quan trọng để chống lại tấn công CSRF. `Strict` là an toàn nhất nhưng có thể gây phiền toái nếu ứng dụng của bạn có các luồng chuyển hướng phức tạp. `Lax` là một lựa chọn cân bằng tốt.
*   **CSRF Protection**: Vì bạn đang dùng `http.csrf(c -> c.disable())`, thuộc tính `SameSite` của cookie là tuyến phòng thủ chính chống lại CSRF. Hãy chắc chắn rằng bạn hiểu rõ về nó.
