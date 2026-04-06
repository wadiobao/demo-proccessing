ể triển khai tính năng "Đọc tiếp", phía Backend (BE) cần xử lý 3 phần chính sau đây:

1. Thiết kế Table trong Database (reading_progress)
Cần một bảng để lưu trữ trạng thái đọc của người dùng. Mỗi khi người dùng cuộn trang, chúng ta sẽ lưu lại "dấu mốc" tại đây:

Trường	Kiểu dữ liệu	Mô tả
user_id	Long	ID người dùng (Foreign Key).
pdf_id	Long	ID tài liệu đang đọc (Foreign Key).
scroll_percent	Double	Vị trí cuộn trang hiện tại (ví dụ: 45.52%).
last_page	Integer	Trang cuối cùng người dùng đang xem (ví dụ: Trang 5).
updated_at	Timestamp	Thời điểm cập nhật cuối cùng (tự động cập nhật).
2. Xử lý Logic "Upsert" (Update or Insert)
Khi Backend nhận dữ liệu từ Frontend (POST /api/v1/progress), logic xử lý nên như sau:

Kiểm tra tồn tại: Tìm xem trong DB đã có bản ghi nào khớp với cặp (user_id, pdf_id) chưa?
Cập nhật (Update): Nếu đã có, chỉ cập nhật giá trị scroll_percent và last_page mới nhất.
Thêm mới (Insert): Nếu chưa có, tạo một bản ghi mới cho tài liệu này.
3. Xây dựng 2 API Endpoints quan trọng
API Lấy vị trí (GET): /api/v1/progress/pdf/{pdfId}
Nhiệm vụ: Khi người dùng mở một file, FE sẽ gọi API này để biết họ đã dừng ở đâu và tự động cuộn đến đó.
API Lưu vị trí (POST): /api/v1/progress
Nhiệm vụ: Nhận dữ liệu { pdfId, scrollPercent, lastPage } từ FE để lưu lại.
