# Frontend Admin API Integration Guide

Tài liệu này hướng dẫn các nhà phát triển Frontend cách tích hợp bộ API Admin mới để quản lý người dùng, tài liệu và nội dung.

## 1. Thông tin chung
- **Base URL**: `/api/v1/admin`
- **Xác thực**: Cần gửi kèm JWT token trong Cookie hoặc Header (tùy cấu hình). Bắt buộc User phải có role `ADMIN`.

## 2. Danh sách API chi tiết

### 2.1. Quản lý Người dùng (User Management)

| Chức năng | Method | Endpoint | Request Body | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| Lấy danh sách | `GET` | `/users?page=0&size=10` | N/A | Trả về đối tượng Page (JPA). |
| Cập nhật Role | `PUT` | `/users/{id}/role` | `["ADMIN", "USER"]` | Gửi mảng các chuỗi Role. |
| Xóa người dùng | `DELETE` | `/users/{id}` | N/A | Xóa vĩnh viễn user khỏi DB SQL. |

> [!TIP]
> **UI Gợi ý**: Sử dụng Component `Table` với phân trang (Pagination). Thêm nút "Edit Role" mở ra một `Dialog` chứa các `Checkbox` để chọn quyền.

---

### 2.2. Quản lý Tài liệu (File Management)

| Chức năng | Method | Endpoint | Request Body | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| Lấy danh sách file | `GET` | `/files?page=0&size=10` | N/A | Thông tin file PDF lưu trong SQL. |
| Xóa 1 file | `DELETE` | `/files/{cloudinaryId}` | N/A | Xóa trong SQL + Cloudinary. |
| Xóa hàng loạt | `DELETE` | `/files/bulk` | `["id1", "id2"]` | Gửi mảng `cloudinaryId`. |

> [!IMPORTANT]
> Khi xóa file, hệ thống sẽ thực hiện xóa đồng bộ trên Cloudinary. Hãy hiển thị một `Toast` thông báo trạng thái "Processing..." vì tác vụ này có thể mất vài giây.

---

### 2.3. Quản lý Câu hỏi & Bình luận (Content Management)

| Chức năng | Method | Endpoint | Request Body | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| Xóa bình luận | `DELETE` | `/comments/{id}` | N/A | Xóa bình luận bài đăng (SQL). |
| DS Câu hỏi đã sinh | `GET` | `/questions/archived` | N/A | Lấy từ MongoDB (ArchivedQuestion). |
| Xóa bộ câu hỏi | `DELETE` | `/questions/archived/{author}` | N/A | Xóa bộ đề gần nhất của author. |
| DS Question Bank | `GET` | `/questions` | N/A | Câu hỏi cộng đồng (MongoDB). |
| Duyệt câu hỏi | `PUT` | `/questions/promote/{contentId}` | N/A | Chuyển trạng thái sang `VERIFIED`. |

---

## 3. Cấu trúc Response mẫu (StateResponse)

Tất cả API đều trả về định dạng chuẩn:
```json
{
  "code": 1000,
  "message": "Success notification",
  "result": { ... data ... }
}
```

## 4. Lưu ý cho UI (Glassmorphism Style)
- **Sidebar**: Cố định bên trái, sử dụng `backdrop-blur-md` và `bg-white/10`.
- **Active State**: Sử dụng gradient nhẹ (ví dụ: `from-blue-500 to-purple-600`) cho các mục đang chọn.
- **Charts**: Sử dụng `Recharts` để vẽ biểu đồ thống kê từ API `/users` hoặc `/files`.
- **Icons**: Sử dụng bộ icon `Lucide React` (User, File, MessageCircle, Settings).

---
> [!NOTE]
> Tài liệu này được tự động cập nhật dựa trên phiên bản API Backend mới nhất ngày 31/03/2026.
