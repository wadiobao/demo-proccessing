# 🧪 Thuật toán & Kỹ thuật Xử lý dữ liệu (Algorithms & Data Guide)

Tài liệu này đi sâu vào các "động cơ" toán học và kỹ năng tối ưu hóa mã nguồn giúp hệ thống hoạt động thông minh và ổn định.

---

## 1. Thuật toán Trắc nghiệm Thích ứng (Adaptive Testing)
Hệ thống không chỉ chấm điểm, mà còn "hiểu" trình độ của bạn để đưa ra câu hỏi phù hợp.

### A. Mô hình IRT (Item Response Theory)
*   **Mô hình 1PL**: Sử dụng hàm Logistic để tính xác suất một người có năng lực $\theta$ trả lời đúng câu hỏi có độ khó $b$.
*   **Cách hoạt động**: Nếu bạn trả lời đúng một câu khó, điểm năng lực ($\theta$) tăng mạnh. Nếu sai câu dễ, $\theta$ giảm sâu.
*   **Vị trí code**: `IRTCalculator.java` -> phương thức `p(theta, b)`.

### B. Ước lượng MAP (Maximum A Posteriori)
*   **Tại sao dùng?**: Để tính toán năng lực người dùng dựa trên lịch sử trả lời nhưng vẫn giữ được sự ổn định (tránh việc điểm số nhảy quá cao hoặc quá thấp sau 1 câu hỏi).
*   **Mô hình Bayes**: Kết hợp dữ liệu thực tế (Likelihood) với một giả định ban đầu (Prior - thường là phân phối Chuẩn).

### C. Tối ưu hóa Newton-Raphson
*   **Mục đích**: Tìm điểm cực đại của hàm xác suất (năng lực chính xác nhất của bạn).
*   **Kỹ thuật**: Sử dụng đạo hàm bậc nhất (Gradient) và đạo hàm bậc hai (Hessian) để "nhảy" dần tới kết quả tối ưu.
*   **Tối ưu**: Sử dụng `dampingFactor` để tránh việc tính toán bị dao động (oscillating) và đặt giới hạn biên $[-4, 4]$ để đảm bảo an toàn số học.

---

## 2. Xử lý Dữ liệu Thông minh (Data Processing)

### A. Tìm kiếm theo Vector & Độ tương đồng (Similarity)
*   **Thuật toán**: **Cosine Similarity**.
*   **Cách dùng**: Tính toán góc giữa hai vector nội dung. Nếu góc càng nhỏ (Cosine càng gần 1), hai nội dung càng giống nhau.
*   **Ứng dụng**: Tìm câu hỏi tương tự hoặc gợi ý chủ đề liên quan.

### B. Chiến lược Xử lý Đa định dạng (Strategy Pattern)
*   **Kỹ năng**: Thay vì dùng chuỗi lệnh `if-else` dài dằng dặc, hệ thống dùng **Strategy Pattern**.
*   **Cách hoạt động**: Tự động nhận diện loại file (PDF, Excel, Word) và chuyển hướng tới "Bộ xử lý" (Processor) tương ứng. Giúp code dễ mở rộng (muốn thêm định dạng mới chỉ cần tạo class mới).

---

## 3. Kỹ năng Tối ưu hóa Hệ thống (System Optimization)

### A. Khóa lạc quan (Optimistic Locking)
*   **Kỹ thuật**: Sử dụng `@Version` trong JPA.
*   **Tối ưu**: Thay vì khóa toàn bộ bảng (Database Lock) gây chậm hệ thống, chúng ta cho phép mọi người đọc dữ liệu thoải mái, nhưng khi lưu, hệ thống sẽ kiểm tra xem có ai vừa sửa trước mình không. Nếu có, nó sẽ báo lỗi và yêu cầu thử lại.

### B. Quản lý Hiệu suất Database
*   **N+1 Query Avoidance**: Sử dụng `join fetch` hoặc MapStruct để lấy dữ liệu liên quan trong 1 lần truy vấn duy nhất, giảm thiểu số lần "gõ cửa" Database.
*   **Sử dụng Redis**: Lưu trữ các kết quả tính toán phức tạp hoặc các lượt giới hạn (quota) vào bộ nhở RAM để có tốc độ phản hồi tính bằng mili giây.

### C. Lập lịch Tự động (Spring Scheduling)
*   **Kỹ năng**: Sử dụng Cron Expression (vd: `0 0 0 1 * ?`).
*   **Ứng dụng**: Tự động thực hiện các tác vụ nặng (như reset điểm uy tín cho hàng ngàn người dùng) vào đêm ngày mùng 1 hàng tháng mà không cần con người can thiệp.

---

## 📈 Tóm tắt quy trình xử lý 1 câu hỏi
1.  **Nhập liệu**: OCR (Tesseract) trích xuất chữ từ ảnh.
2.  **Làm sạch**: `PromptSanitizer` loại bỏ các ký tự rác.
3.  **Tác vụ AI**: Gửi nội dung đã sạch qua Gemini AI để phân loại Bloom Level.
4.  **Lưu trữ**: Map qua DTO và lưu vào MongoDB (NoSQL) để tận dụng cấu trúc linh hoạt.
5.  **Phản hồi**: Tính toán lại độ khó câu hỏi dựa trên phản hồi của người dùng bằng IRT.
