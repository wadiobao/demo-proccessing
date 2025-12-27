# Hệ thống tạo đề thi trắc nghiệm bằng AI

## Mô tả

Đây là một dự án ứng dụng Spring Boot được thiết kế để tạo đề thi trắc nghiệm bằng trí tuệ nhân tạo. Chức năng chính của hệ thống là xử lý các tệp PDF được người dùng tải lên, trích xuất nội dung (sử dụng OCR nếu cần thiết), và sau đó sử dụng Google Gemini AI để tạo ra một bộ câu hỏi trắc nghiệm dựa trên nội dung đó. Đề thi cuối cùng sẽ được cung cấp dưới dạng các tệp Word và PDF có thể tải xuống.

Ứng dụng có kiến trúc đa cơ sở dữ liệu (MySQL, MongoDB, Redis) và hệ thống xác thực người dùng an toàn dựa trên JWT với quy trình đăng ký hai bước sử dụng OTP.

## Công nghệ sử dụng

- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot
- **Build Tool:** Maven
- **Cơ sở dữ liệu:**
  - MySQL
  - MongoDB
  - Redis
- **AI & OCR:**
  - Google GenAI
  - Tess4J (Tesseract OCR)
- **Xác thực:** Spring Security, JWT

## Tính năng chính

- **Tạo đề thi bằng AI:** Tự động tạo câu hỏi trắc nghiệm từ nội dung của tệp PDF.
- **Trích xuất văn bản OCR:** Hỗ trợ nhận dạng và trích xuất văn bản từ hình ảnh trong tệp PDF.
- **Tải xuống đề thi:** Cung cấp đề thi ở định dạng `.docx` (Word) và `.pdf`.
- **Xác thực an toàn:** Sử dụng JWT (JSON Web Tokens) để xác thực và phân quyền người dùng.
- **Đăng ký hai bước:** Quy trình đăng ký người dùng mới thông qua xác thực OTP qua email.

## Hướng dẫn cài đặt và chạy dự án

### Yêu cầu tiên quyết

- JDK 17 hoặc mới hơn
- Maven
- Cấu hình các cơ sở dữ liệu (MySQL, MongoDB, Redis)

### Cài đặt

1. **Clone repository:**
   ```sh
   git clone <URL_CUA_REPOSITORY>
   cd <TEN_THU_MUC_DU_AN>
   ```

2. **Cấu hình môi trường:**
   - Tạo một tệp `.env` ở thư mục gốc của dự án.
   - Sao chép nội dung từ tệp `.env.example` và điền các giá trị thực tế cho các biến môi trường như thông tin kết nối cơ sở dữ liệu, API key của Google, v.v.

3. **Build dự án:**
   Sử dụng Maven Wrapper để build dự án:
   ```sh
   ./mvnw clean install
   ```
   (Hoặc `mvnw.cmd clean install` trên Windows)

4. **Chạy dự án:**
   Sau khi build thành công, chạy ứng dụng bằng lệnh:
   ```sh
   ./mvnw spring-boot:run
   ```
   (Hoặc `mvnw.cmd spring-boot:run` trên Windows)

## API Endpoints

Dưới đây là một số API endpoint chính của hệ thống:

- `POST /api/handlepdf`: Endpoint công khai để tạo đề thi từ PDF.
- `POST /api/handlepdf/private`: Endpoint riêng tư (yêu cầu xác thực) để tạo đề thi.
- `POST /auth/login`: Đăng nhập người dùng và nhận token JWT.
- `POST /auth/refresh`: Làm mới (refresh) token JWT.
- `POST /user/register`: Bắt đầu quá trình đăng ký người dùng mới.
- `POST /user/register/otp`: Xác thực OTP để hoàn tất đăng ký.
