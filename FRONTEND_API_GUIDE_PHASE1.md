# Hướng dẫn tích hợp API mới cho Frontend (Phase 1)

Tài liệu này hướng dẫn cách sử dụng các API mới triển khai trong Giai đoạn 1: Bảo mật & Quản lý người dùng.

## 1. Quên mật khẩu (Forgot Password)
Sử dụng khi người dùng không nhớ mật khẩu và yêu cầu mã khôi phục.

- **Endpoint:** `POST /api/v1/user/forgot-password`
- **Body:**
```json
{
  "email": "user@example.com"
}
```
- **Phản hồi thành công:** Trả về tin nhắn xác nhận OTP đã được gửi.

## 2. Đặt lại mật khẩu (Reset Password)
Sử dụng sau khi người dùng nhận được OTP từ Email.

- **Endpoint:** `POST /api/v1/user/reset-password`
- **Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "new_secure_password"
}
```
- **Lưu ý:** Hệ thống sẽ tự động xác thực OTP và cập nhật mật khẩu mới.

## 3. Cập nhật hồ sơ cá nhân (Update Profile)
Sử dụng để cập nhật thông tin bổ sung (Avatar, Chuyên ngành).

- **Endpoint:** `PUT /api/v1/user/profile`
- **Header:** 
    - `Authorization: Bearer <JWT_TOKEN>`
    - `Content-Type: multipart/form-data`
- **Body (Form Data):**
    - `avatar`: File ảnh (tùy chọn)
- **Phản hồi:** Trả về đối tượng `UserResponse` đã được cập nhật.

## 4. Đổi mật khẩu (Change Password)
Sử dụng bên trong trang cài đặt tài liệu khi người dùng đã đăng nhập.

- **Endpoint:** `PUT /api/v1/user/change-password`
- **Header:** `Authorization: Bearer <JWT_TOKEN>`
- **Body:**
```json
{
  "oldPassword": "current_password",
  "newPassword": "new_secure_password"
}
```

---
**Ghi chú chung:**
- Tất cả các yêu cầu thay đổi mật khẩu (`newPassword`) phải đảm bảo ít nhất 8 ký tự.
- Các API Profile và Change Password yêu cầu JWT Token hợp lệ trong Header.
