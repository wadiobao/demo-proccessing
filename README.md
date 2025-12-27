# Hệ thống tạo đề thi trắc nghiệm bằng AI

## Mô tả

Dự án này là một giải pháp toàn diện giúp chuyển đổi tài liệu học tập và tài liệu tham khảo từ định dạng PDF thành các bộ đề thi trắc nghiệm tương tác một cách tự động. Mục tiêu chính là tiết kiệm thời gian cho giáo viên, giảng viên và người dùng trong việc tạo ra các bài kiểm tra, ôn tập kiến thức.

**Ứng dụng có thể làm được những gì:**

*   **Phân tích và tạo câu hỏi tự động:** Người dùng chỉ cần tải lên một tệp PDF (ví dụ: bài giảng, sách giáo khoa, tài liệu nghiên cứu). Hệ thống sẽ tự động phân tích nội dung và sử dụng sức mạnh của mô hình ngôn ngữ lớn (Google Gemini AI) để tạo ra các câu hỏi trắc nghiệm phù hợp và đa dạng, bám sát vào nội dung gốc.

*   **Nhận dạng ký tự quang học (OCR):** Đối với các tài liệu được quét (scanned) hoặc chứa hình ảnh có văn bản, hệ thống tích hợp công nghệ OCR (Tess4J) để trích xuất và số hóa nội dung, đảm bảo không bỏ sót thông tin quan trọng.

*   **Tùy chọn định dạng đầu ra linh hoạt:** Đề thi sau khi được tạo có thể được xuất ra hai định dạng phổ biến:
    *   **`.docx` (Microsoft Word):** Cho phép người dùng dễ dàng chỉnh sửa, bổ sung hoặc tùy biến lại bộ câu hỏi trước khi sử dụng.
    *   **`.pdf`:** Thuận tiện cho việc chia sẻ, in ấn và sử dụng trên nhiều thiết bị.

*   **Quản lý người dùng an toàn:** Hệ thống cung cấp cơ chế đăng ký và đăng nhập an toàn. Dữ liệu người dùng được bảo vệ thông qua quy trình đăng ký hai bước (xác thực OTP) và xác thực dựa trên token (JWT) cho các phiên làm việc sau đó.

*   **Kiến trúc hệ thống mạnh mẽ:** Phía sau, ứng dụng được xây dựng trên một kiến trúc vững chắc, kết hợp nhiều loại cơ sở dữ liệu để tối ưu hóa hiệu suất: MySQL để quản lý dữ liệu người dùng có cấu trúc, MongoDB để lưu trữ các bộ đề thi linh hoạt, và Redis để quản lý cache và phiên làm việc.

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
