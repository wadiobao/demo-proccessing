# Hướng dẫn chi tiết API (Technical Reference)

Tài liệu này cung cấp chi tiết kỹ thuật về cách gọi, tham số yêu cầu (Request Body) và cấu trúc phản hồi (Response Body) của các API trong hệ thống.

---

## 1. Cấu trúc phản hồi chuẩn (Standard Response)

Tất cả các API đều trả về một object chuẩn `StateResponse`:

```json
{
  "code": 1000,
  "message": "Thông báo (nếu có)",
  "result": { ... } // Dữ liệu trả về cụ thể của từng API
}
```

- **code**:
  - `1000`: Thành công.
  - `1xxx - 5xxx`: Các mã lỗi (Xem chi tiết ở mục cuối).
- **result**: Chứa dữ liệu thực tế hoặc null nếu chỉ cần thông báo thành công.

---

## 2. Nhóm Authentication & User (Công khai)

### 🟢 Đăng ký tài khoản
- **Endpoint**: `POST /api/v1/user/register`
- **Request Body** (`UserRequest`):
  ```json
  {
    "userName": "string (min 5 chars)",
    "password": "string (min 8 chars)",
    "email": "string (valid email format)",
    "date": "dd/MM/yyyy (min 16 years old)"
  }
  ```
- **Response**: Trả về thông báo "OTP sent" và gửi mã xác thực về email.

### 🟢 Xác thực OTP Đăng ký
- **Endpoint**: `POST /api/v1/user/register/otp`
- **Request Body**:
  ```json
  {
    "email": "string",
    "otp": "string"
  }
  ```
- **Response**: Trả về thông tin User nếu xác thực thành công.

### 🟢 Đăng nhập
- **Endpoint**: `POST /api/v1/auth/login`
- **Request Body**:
  ```json
  {
    "userName": "string",
    "password": "string"
  }
  ```
- **Response**: Trả về thông tin đăng nhập thành công trong result. Token được trả về qua **Cookies**: `access-token` và `refresh-token`.
  ```json
  {
    "code": 1000,
    "result": {
      "auth": true,
      "reputationScore": 150, // [MỚI] Điểm uy tín hiện tại
      "roleTier": "CONTRIBUTOR" // [MỚI] Cấp bậc (CONTRIBUTOR, EXPERT, MODERATOR...)
    }
  }
  ```

---

## 3. Nhóm Quiz (Trắc nghiệm)

### 🟢 Tạo bài thi công khai (Public)
- **Endpoint**: `POST /api/v1/quiz/public`
- **Content-Type**: `multipart/form-data`
- **Tham số (Request Parameters)**:
  - `file`: Tệp PDF/Docx/Ảnh (Tài liệu gốc).
  - `questionCount`: Số lượng câu (mặc định 10).
  - `level`: Độ khó (0: Dễ, 1: Khó, 2: Thích ứng).
  - `type`: Loại kiến thức (0: Ghi nhớ, 1: Áp dụng).
  - `language`: Ngôn ngữ (vietnamese/english).
- **Response**: Trả về danh sách câu hỏi AI vừa tạo. Không lưu vào database.

### 🔴 Tạo bài thi cá nhân (Private)
- **Endpoint**: `POST /api/v1/quiz/private`
- **Auth**: Token required.
- **Tham số**: Giống Public nhưng có thêm tham số `topic` (chủ đề) để lưu lịch sử.
- **Response**: Trả về danh sách câu hỏi và **lưu vào tài khoản người dùng**.

### 🔴 Nộp bài thi (Submit)
- **Endpoint**: `POST /api/v1/quiz/submit`
- **Auth**: Token required.
- **Request Body**:
  ```json
  {
    "archivedQuestionId": "string (ID của bộ câu hỏi)",
    "topic": "string",
    "answers": [
      { "id": 1, "answer": "A" },
      { "id": 2, "answer": "C" }
    ]
  }
  ```
- **Response** (`QuizSubmissionResponse`):
  ```json
  {
    "totalQuestions": 10,
    "correctAnswers": 8,
    "scorePercentage": 80.0,
    "newTheta": 0.5, // Chỉ số năng lực IRT mới của user
    "feedback": "Khá tốt! Bạn hãy tập trung thêm vào..."
  }
  ```

---

## 4. Nhóm Thảo luận & Cộng đồng (Discussion)

### 🟢 Xem danh sách Topic/Form
- **Endpoint**: `GET /api/v1/discussion` (hoặc `/topics`, `/forms`)
- **Tham số**: `page` (trang), `size` (số lượng).
- **Response** (`Page<FormResponse>`):
  ```json
  {
    "formId": "uuid-string",
    "tacGia": "username",
    "tieuDe": "Tiêu đề bài đăng",
    "tags": ["tag1", "tag2"],
    "ngayDang": "ISO-date",
    "noiDung": "Nội dung bài viết",
    "topic": "Chủ đề",
    "voteScore": 15,       // [MỚI] Tổng điểm vote hiện tại
    "userVoteValue": 1     // [MỚI] Trạng thái của user hiện tại (1: Up, -1: Down, 0: None)
  }
  ```

### 🔴 Tìm kiếm Thảo luận (FTS)
- **Endpoint**: `GET /api/v1/discussion/search`
- **Tham số**: `keyword` (từ khóa cần tìm), `page`, `size`.
- **Đặc điểm**: Sử dụng MySQL Full-Text Search trên Tiêu đề và Nội dung.
- **Response**: Trả về danh sách `FormResponse` tương tự như danh sách Topic.

### 🔴 Đóng góp câu hỏi bằng AI (Bulk Upload)
- **Endpoint**: `POST /api/v1/discussion/upload-questions`
- **Auth**: Token (Role Tier > RESTRICTED).
- **Request Parameters**:
  - `file`: Document chứa câu hỏi.
- **Response**: Danh sách các câu hỏi đã được AI nhận diện và lưu vào ngân hàng cộng đồng.

### 🔴 Đánh giá bài đăng (Vote)
- **Endpoint**: `POST /api/v1/discussion/{formId}/vote`
- **Auth**: Token required.
- **Query Param**: `value` (1: Upvote, -1: Downvote, 0: Un-vote).
- **Response**: Cập nhật uy tín (Reputation) của tác giả bài đăng.

---

## 5. Nhóm Ngân hàng câu hỏi (Question Bank)

### 🔴 Tìm kiếm câu hỏi cộng đồng
- **Endpoint**: `GET /api/v1/question-bank/search`
- **Tham số**: `keyword` (nội dung câu hỏi), `page`, `size`.
- **Đặc điểm**: Sử dụng MongoDB Text Index để tìm kiếm mờ trong nội dung câu hỏi.
- **Response**: Trả về danh sách các câu hỏi trắc nghiệm phù hợp.

---

## 5. Danh mục mã lỗi (Error Codes)

| Mã lỗi | Thông báo | Ý nghĩa |
| :--- | :--- | :--- |
| **1000** | Success | Thành công |
| **2001** | Unauthenticated | Chưa đăng nhập hoặc Token hết hạn |
| **2002** | Unauthorized | Không đủ thẩm quyền (Ví dụ: Tier RESTRICTED không được upload) |
| **3001** | User Existed | Username hoặc Email đã được sử dụng |
| **3002** | User Not Found | Không tìm thấy người dùng |
| **3006** | Invalid Date | Người dùng chưa đủ 16 tuổi |
| **4003** | File Too Large | Tệp tải lên vượt quá giới hạn hệ thống |
| **5001** | Gen Failed | Lỗi trong quá trình AI xử lý tài liệu |
