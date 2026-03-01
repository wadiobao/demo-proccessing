# Frontend API Guide - Phase 3 (Adaptive Learning)

Tài liệu này hướng dẫn cách tích hợp các tính năng học tập thích ứng mới vào giao diện người dùng.

## 1. Sinh đề thi thích ứng (Adaptive Quiz)

Sử dụng endpoint `/private` với các tham số mới để kích hoạt cá nhân hóa.

- **URL**: `POST /api/v1/quiz/private`
- **Params**:
    - `file`: (MultipartFile)
    - `level`: `2` (Bắt buộc chọn 2 để bật chế độ Adaptive)
    - **[NEW]** `topic`: (String, optional) - Chủ đề học tập. Nếu để trống, hệ thống sẽ tự dùng AI để nhận diện.
- **Cơ chế**: Hệ thống sẽ tự tra cứu lịch sử của User theo topic này để sinh ra các câu hỏi có độ khó phù hợp nhất.

## 2. Gửi kết quả & Cập nhật năng lực

Hệ thống sẽ tự động cập nhật chỉ số IRT ngay khi bạn gửi bài.

- **URL**: `POST /api/v1/quiz/submit`
- **Body**: `QuizSubmissionRequest`
- **Response**: Trả về `newTheta` và `newDifficulty` cùng feedback cá nhân hóa.

## 3. Lấy dữ liệu thống kê (Dashboard)

Dùng để vẽ biểu đồ tiến độ hoặc hiển thị trình độ hiện tại.

- **URL**: `GET /api/v1/quiz/stats`
- **Query Params**:
    - `topic`: (String) - Tên chủ đề cần xem thống kê.
- **Dữ liệu trả về**:
```json
{
  "result": {
    "username": "user123",
    "topic": "toán học",
    "theta": 1.25,        // Năng lực hiện tại (-4.0 đến 4.0)
    "difficulty": 0.8,     // Độ khó đề xuất tiếp theo
    "totalQuizzes": 5,
    "totalQuestionsAnswered": 50,
    "accuracyPercentage": 82.0,
    "recentHistory": [...], // 20 câu trả lời gần nhất
    "bloomStats": {         // Dữ liệu dùng vẽ Lược đồ kỹ năng (Radar Chart)
      "Remembering": 90.0,
      "Understanding": 85.5,
      "Applying": 70.0,
      "Analyzing": 60.5
    }
  }
}
```

## Lưu ý cho Frontend
- Khi người dùng chọn topic cũ, hệ thống sẽ tiếp nối lộ trình. 
- Khi người dùng nhập topic mới, hệ thống sẽ thực hiện "Cold Start" (bắt đầu từ mức trung bình).
- Hãy khuyến khích người dùng nhập topic rõ ràng để việc tracking chính xác hơn.

---

## 4. Tài liệu Thiết kế & Tích hợp Dashboard (Adaptive Learning)

Để giúp Frontend xây dựng trang thống kê Học tập thích ứng (Dashboard), dưới đây là hướng dẫn về UI/UX và các API tương ứng.

### 4.1. Cấu trúc UI/UX đề xuất

Trang Dashboard nên được chia thành 3 phân vùng chính để người dùng dễ quan sát tiến độ:

1. **VÙNG TỔNG QUAN (Hero / KPI Cards):**
   - **🏆 Cấp độ Kỹ năng Tổng thể**: Hiển thị điểm từ 0-100 (Ví dụ: `65/100`). Chuyển đổi màu sắc hoặc danh hiệu (Tân binh, Thành thạo, Chuyên gia).
   - **📚 Chủ đề đã học**: Đếm tổng số Topic mà user đã tham gia trả lời.
   - **🎯 Tỷ lệ chính xác chung**: Hiển thị % trả lời đúng trên toàn bộ hệ thống.
   - **📈 Tổng số câu đã làm**: Số lượng câu hỏi user đã rèn luyện.

2. **VÙNG TRỰC QUAN HÓA (Charts - Dùng Recharts hoặc Chart.js):**
   - **Biểu đồ Mạng nhện (Mức độ thành thạo - Radar Chart)**: Hiển thị các "Topic" ở các trục, và "Mastery Level" (0-100) làm giá trị lõi. Giúp user nhận ra mình mạnh yếu môn nào.
   - *(Option)* **Biểu đồ Tư duy Bloom (Radar Chart phụ)**: Hiển thị khả năng Nhận biết/Vận dụng/Phân tích (Lấy từ API `stats?topic=...` của từng chủ đề riêng lẻ).

3. **VÙNG HOẠT ĐỘNG GẦN ĐÂY (Recent Activity / History):**
   - Danh sách các bộ câu hỏi / đề thi gần nhất mà người dùng đã làm. Hiển thị dưới dạng List hoặc Table.
   - Cung cấp nút `[Xem chi tiết]` hoặc `[Làm lại đề này]`.

### 4.2. Danh sách API phục vụ Dashboard

#### A. Lấy Số liệu Tổng quan (Dùng cho Vùng 1 & 2)

Quét tất cả dữ liệu hệ thống học tập của người dùng.

- **URL**: `GET /api/v1/quiz/stats/overview`
- **Output (Json)**:
```json
{
  "code": 1000,
  "result": {
    "username": "user123",
    "totalTopicsMastered": 5,        // Số chủ đề đã học
    "overallSkillLevel": 65.5,       // Cấp độ hệ số (0-100)
    "overallAccuracyPercentage": 78.5, // % Trả lời đúng
    "totalQuestionsAnswered": 120,   // Tổng câu đã làm
    "radarChartData": [              // Data đút vào Library Radar Chart
      { "topic": "Đại số", "masteryLevel": 80.0 },
      { "topic": "Hình học", "masteryLevel": 50.0 }
    ]
  },
  "message": "Lấy tổng quan học tập thành công"
}
```

#### B. Lấy Lịch sử Làm bài (Dùng cho Vùng 3)

Lấy lại danh sách các bộ đề câu hỏi (PDF) mà người dùng từng tương tác.

- **URL**: `GET /api/v1/mongo/author?author={username}`
- **Giải thích**: Trả về các đối tượng `ArchivedQuestion` chứa thông tin file đã làm, danh sách các `Question`, thời gian (`createdAt`). Frontend bóc tách để làm List lịch sử ôn tập.

#### C. Lấy Số liệu Chi tiết của 1 Chủ đề (Drill-down)

Khi User bấm vào 1 môn học cụ thể trên Radar Chart, có thể gọi API này để xem sâu hơn.

- **URL**: `GET /api/v1/quiz/stats?topic={topic_name}`
- **Giải thích**: Trả về điểm Theta gốc, Tỷ lệ chính xác của riêng chủ đề đó và **`bloomStats`** (Điểm số theo thang Bloom - Nhận biết, Hiểu, Vận dụng) dùng để vẽ Lược đồ nhận thức.
