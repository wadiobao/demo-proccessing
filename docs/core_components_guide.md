# 🏗️ Các Thành phần Cốt lõi (Core Components Guide)

Tài liệu này bản đồ hóa các Class và Method (Hàm) quan trọng nhất trong dự án, giúp bạn biết được "trái tim" của hệ thống nằm ở đâu.

---

## 1. Công cụ Tính toán & Thuật toán (Utilities)
Đây là nơi chứa các logic xử lý chuyên sâu về toán học và AI.

### `IRTCalculator`
*   **Hàm `estimateThetaMAP`**: Ước lượng năng lực thực tế của người dùng sau mỗi bài kiểm tra.
*   **Hàm `recalibrateItemDifficulty`**: Tự động điều chỉnh độ khó của một câu hỏi dựa trên việc người dùng làm đúng hay sai.
*   **Hàm `p(theta, b)`**: Tính xác suất một người làm đúng một câu hỏi cụ thể.

### `DocumentProcessorContext` & `IDocumentProcessor`
*   **Vai trò**: Triển khai Strategy Pattern để phân loại xử lý file.
*   **Các lớp con**: `PdfProcessor`, `ExcelProcessor`, `DocxProcessor`, `TxtProcessor`.
*   **Hàm `extractText`**: Trích xuất toàn bộ văn bản từ file đầu vào để chuẩn bị cho AI.

---

## 2. Tầng Nghiệp vụ (Service Layer)
Nơi chứa các quy trình xử lý dữ liệu chính (Flow).

### `QuizService` & `QuizAnswerService`
*   **Hàm `processPrivateQuiz`**: Quy trình tạo bộ câu hỏi riêng tư (lưu vào DB) kèm tính năng giới hạn chủ đề (Topic).
*   **Hàm `submitQuizAnswers`**: Tiếp nhận bài làm, tính điểm và kích hoạt `IRTCalculator` để cập nhật trình độ người dùng.

### `ReputationService`
*   **Hàm `castVote`**: Xử lý việc cộng/trừ điểm uy tín khi người dùng bầu chọn cho nhau. Đảm bảo tính nhất quán (không cho phép vote 2 lần trên cùng 1 bài).
*   **Hàm `determineTier`**: Quyết định cấp bậc người dùng (`EXPERT`, `CONTRIBUTOR`...) dựa trên điểm số hiện có.
*   **Hàm `performMonthlyReset`**: Thuật toán quét và xóa nợ (điểm âm) định kỳ hàng tháng.

### `BulkQuestionUploadService`
*   **Hàm `uploadQuestionsFromExcel`**: Đọc file Excel, bóc tách câu hỏi, đáp án và lời giải (Explanation) để nạp vào ngân hàng câu hỏi cộng đồng.

---

## 3. Quản lý Dữ liệu & Ngân hàng câu hỏi
### `QuestionBankService`
*   **Hàm `updateQuestion`**: Cho phép chỉnh sửa nội dung câu hỏi trong kho dữ liệu chung.
*   **Hàm `checkAndResetQuota`**: Kiểm tra và kiểm soát giới hạn chỉnh sửa (tối đa 5 câu/ngày) để bảo vệ tính toàn vẹn của dữ liệu.

### `ContentService`
*   **Vai trò**: Quản lý các bài đăng thảo luận và các nội dung đa phương tiện gắn kèm.

---

## 4. Tầng Điều khiển (API Controllers)
Nơi tiếp nhận yêu cầu từ Frontend và định hướng xử lý.

*   **`QuizController`**: Quản lý toàn bộ vòng đời của một bài trắc nghiệm (Tạo mới -> Làm bài -> Nộp bài -> Xem thống kê).
*   **`FormController`**: Cổng vào cho các thảo luận cộng đồng, hệ thống Vote và Upload file Excel.
*   **`UserController`**: Xử lý Hồ sơ cá nhân, Bảo mật tài khoản và hệ thống OTP.
*   **`QuestionBankController`**: API dành cho việc cập nhật/vận hành kho câu hỏi.

---

## 5. Bảo mật & Cấu hình (Infrastructure)
*   **`SecurityConfig`**: "Cánh cổng" bảo mật, nơi cấu hình JWT, bộ lọc Cookie và phân quyền đường dẫn API.
*   **`ApplicationInitConfig`**: Tự động tạo dữ liệu mẫu và các tài khoản hệ thống khi ứng dụng khởi chạy lần đầu.

---

### 🔍 Làm sao để tìm code nhanh?
1.  Nhấn `Ctrl + N` (trong IntelliJ) hoặc `Ctrl + P` (trong VS Code).
2.  Gõ tên Class hoặc tên Hàm đã nêu ở trên.
3.  Đọc Javadoc phía trên mỗi hàm để hiểu lý do tại sao hàm đó tồn tại.
