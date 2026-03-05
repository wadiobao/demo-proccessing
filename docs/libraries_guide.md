# 📚 Hướng dẫn về các Thư viện trong dự án (Third-party Libraries Guide)

Tài liệu này liệt kê và giải thích vai trò của các thư viện quan trọng được sử dụng trong dự án, giúp bạn hiểu rõ "hệ sinh thái" kỹ thuật đang vận hành ứng dụng.

---

## 1. Spring Boot Starters (Bộ khung nền tảng)
Spring Boot giúp đơn giản hóa việc cấu hình và phát triển ứng dụng Java.

*   `spring-boot-starter-web`: Cung cấp các công cụ để xây dựng Web API (RESTful) và nhúng ứng dụng vào máy chủ Tomcat.
*   `spring-boot-starter-data-jpa`: Hỗ trợ giao tiếp với cơ sở dữ liệu quan hệ (MySQL) thông qua chuẩn Java Persistence API.
*   `spring-boot-starter-data-mongodb`: Hỗ trợ lưu trữ và truy vấn dữ liệu dạng tài liệu (JSON) trong MongoDB.
*   `spring-boot-starter-data-redis`: Hỗ trợ lưu trữ dữ liệu trong bộ nhớ (Cache) để tăng tốc độ xử lý hoặc giới hạn tần suất.
*   `spring-boot-starter-validation`: Kiểm tra tính hợp lệ của dữ liệu đầu vào (ví dụ: email phải đúng định dạng, không được để trống).
*   `spring-boot-starter-mail`: Cung cấp tính năng gửi email (dùng cho việc gửi OTP đăng ký hoặc khôi phục mật khẩu).
*   `spring-boot-starter-actuator`: Giám sát sức khỏe của hệ thống (Health Check) và các thông số vận hành.

---

## 2. Bảo mật & Xác thực (Security & Auth)
*   **nimbus-jose-jwt**: Thư viện mạnh mẽ để xử lý mã hóa, giải mã và xác thực JSON Web Tokens (JWT).
*   **spring-security-crypto**: Cung cấp các thuật toán mã hóa mật khẩu an toàn (như BCrypt).

---

## 3. Trí tuệ nhân tạo (AI & LLM)
*   **google-genai**: SDK chính thức của Google để tương tác với mô hình Gemini AI, dùng để phân tích tài liệu và tạo câu hỏi tự động.
*   **langchain4j**: Một bộ khung (framework) giúp tích hợp các mô hình ngôn ngữ lớn (LLM) vào ứng dụng Java một cách linh hoạt, hỗ trợ cả lưu trữ vector (Vector Stores) cho việc tìm kiếm thông minh.

---

## 4. Xử lý tài liệu & Hình ảnh (Document & Image Processing)
Dự án có khả năng đọc nội dung từ nhiều định dạng file khác nhau.

*   **Apache POI**: Thư viện chuẩn để làm việc với các file Microsoft Office (Excel .xlsx, Word .docx).
*   **PDFBox** & **iText**: Các công cụ mạnh mẽ để trích xuất nội dung hoặc tạo mới các file PDF.
*   **Tess4J (Tesseract OCR)**: Thư viện nhận diện ký tự quang học, giúp chuyển đổi hình ảnh chứa chữ thành văn bản thuần túy.
*   **docx4j**: Xử lý chuyên sâu định dạng XML bên trong các tệp Word.

---

## 5. Tiện ích & Công cụ bổ trợ (Utilities)
*   **Lombok**: Giúp giảm thiểu code mẫu (Boilerplate code) bằng cách tự động tạo Getter/Setter qua các Annotation.
*   **MapStruct**: Tự động hóa việc chuyển đổi dữ liệu giữa các đối tượng Entity (Dữ liệu DB) và DTO (Dữ liệu gửi về Frontend).
*   **Cloudinary Java SDK**: Giao tiếp với dịch vụ Cloudinary để lưu trữ và quản lý hình ảnh người dùng tải lên.
*   **Commons IO** & **FileUpload**: Các tiện ích của Apache giúp xử lý luồng (Stream) và tải file lên hệ thống dễ dàng hơn.
*   **spring-dotenv**: Giúp ứng dụng đọc các biến môi trường từ file `.env` (thường dùng để lưu khóa bí mật).

---

## 6. Kết nối Cơ sở dữ liệu (Connectors)
*   **mysql-connector-java**: Trình điều khiển (driver) giúp Java kết nối và ra lệnh cho cơ sở dữ liệu MySQL.
*   **lettuce-core**: Thư viện kết nối Redis với hiệu suất cao, hỗ trợ lập trình bất đồng bộ.

---

### 💡 Lưu ý về phiên bản:
Tất cả các thư viện này được quản lý tập trung trong file `pom.xml`. Khi muốn thêm hoặc cập nhật thư viện, bạn nên kiểm tra tính tương thích với phiên bản **Spring Boot 3.4.x** đang dùng.
