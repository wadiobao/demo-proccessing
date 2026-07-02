# 📄 AI Quiz Generator — Hệ thống tạo đề thi trắc nghiệm bằng AI

> Tự động chuyển đổi tài liệu PDF thành bộ đề thi trắc nghiệm chất lượng cao, sử dụng sức mạnh của Google Gemini AI và OCR.

---

## 📌 Mục lục

- [Tổng quan](#-tổng-quan)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Tính năng](#-tính-năng)
- [Hệ thống học thích ứng IRT](#-hệ-thống-học-thích-ứng-irt)
- [Công nghệ sử dụng](#-công nghệ-sử-dụng)
- [Cài đặt & Chạy local](#-cài-đặt--chạy-local)
- [Triển khai với Docker](#-triển-khai-với-docker)
- [Triển khai với Kubernetes](#-triển-khai-với-kubernetes)
- [Cấu hình môi trường](#-cấu-hình-môi-trường)
- [API Reference](#-api-reference)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)

---

## 🧭 Tổng quan

**AI Quiz Generator** là một RESTful API backend được xây dựng bằng **Spring Boot 3**, giúp giảng viên và người dùng tạo đề thi trắc nghiệm hoàn toàn tự động từ tài liệu PDF. Người dùng chỉ cần tải lên một file PDF — hệ thống sẽ phân tích nội dung, trích xuất văn bản (kể cả từ ảnh quét), gửi đến Google Gemini AI để sinh câu hỏi, rồi xuất ra file .docx hoặc .pdf sẵn sàng sử dụng.

---

## 🏗 Kiến trúc hệ thống

```
Client (HTTP Request)
       │
       ▼
Spring Boot API (Port 8080)
       │
       ├── MySQL       — Quản lý người dùng, phân quyền
       ├── MongoDB     — Lưu trữ bộ đề thi linh hoạt
       ├── Redis       — Cache OTP, JWT blacklist, session
       └── Cloudinary  — Lưu trữ file tải lên
       │
       ├── Google Gemini AI   — Sinh câu hỏi trắc nghiệm
       ├── Tess4J (Tesseract) — OCR trích xuất văn bản từ ảnh
       ├── Apache PDFBox      — Xử lý & phân tích nội dung PDF
       └── IRT Engine (1PL Rasch + MAP) — Đánh giá năng lực & thích ứng đề thi
```

---

## ✨ Tính năng

| Tính năng | Mô tả |
|---|---|
| 🤖 **AI Question Generation** | Tự động sinh câu hỏi trắc nghiệm từ nội dung PDF bằng Google Gemini |
| 🔍 **OCR Support** | Nhận dạng văn bản từ tài liệu quét bằng Tesseract OCR (hỗ trợ tiếng Việt) |
| 📝 **Export linh hoạt** | Xuất đề thi sang .docx (Word) hoặc .pdf |
| 🔐 **JWT Authentication** | Xác thực stateless với Access Token & Refresh Token |
| 📧 **OTP Registration** | Đăng ký tài khoản hai bước qua xác thực email OTP |
| 🔑 **Google OAuth2** | Đăng nhập nhanh bằng tài khoản Google |
| 🧠 **LangChain4j + Embeddings** | Tích hợp embedding (all-MiniLM-L6-v2) để tìm kiếm ngữ nghĩa |
| 📊 **Swagger UI** | Tài liệu API tương tác tại /swagger-ui.html |
| 🎯 **Adaptive IRT Engine** | Đánh giá năng lực thực sự (Theta), tự động điều chỉnh độ khó đề thi theo từng người |
| 📈 **ELO Scoring** | Hệ thống điểm ELO 0–1200 ánh xạ từ Theta, phản ánh tiến bộ theo Bloom's Taxonomy |
| 🐳 **Docker & K8s ready** | Hỗ trợ triển khai containerized với Kubernetes |

---

## 🎯 Hệ thống học thích ứng IRT

Dự án tích hợp một **IRT Engine** tự xây dựng dựa trên mô hình **1-Parameter Logistic (1PL) Rasch** kết hợp với **Maximum A Posteriori (MAP) Estimation** để cá nhân hóa hoàn toàn trải nghiệm thi trắc nghiệm cho từng người dùng.

### Mô hình toán học

Xác suất người dùng trả lời đúng một câu hỏi được tính theo công thức Rasch:

```
P(đúng | θ, b) = 1 / (1 + e^(-(θ - b)))
```

| Ký hiệu | Ý nghĩa |
|---|---|
| `θ` (Theta) | Năng lực thực sự của người dùng, thang đo `[-3.0, +3.0]` |
| `b` | Độ khó câu hỏi trên cùng thang đo |
| `P` | Xác suất trả lời đúng (0.0 – 1.0) |

### Luồng hoạt động

```
[Người dùng nộp bài]
        │
        ▼
1. Tính điểm thô (correctAnswers / totalQuestions)
        │
        ▼
2. MAP Estimation (Newton-Raphson, tối đa 100 vòng lặp)
   → Cập nhật θ mới dựa trên toàn bộ lịch sử làm bài
        │
        ▼
3. Recalibrate Question Bank
   → Điều chỉnh b (độ khó) từng câu hỏi theo SGD
   → Lưu hàng loạt vào MongoDB (Bulk Write)
        │
        ▼
4. Ánh xạ θ → Mastery Level (1–6) → ELO (0–1200)
        │
        ▼
5. Lưu ThetaSnapshot + cập nhật UserResource
        │
        ▼
[Trả về: newElo, deltaElo, oldMastery, newMastery, leveledUp]
```

### Tích hợp Bloom's Taxonomy

Thang đo Theta được chia thành **6 cấp độ tư duy Bloom** với biên giới không đều (hiệu chỉnh từ dữ liệu thực nghiệm):

| Level | Bloom | Theta tối thiểu | Theta tối đa |
|:---:|---|:---:|:---:|
| 1 | Remembering (Ghi nhớ) | -3.00 | -1.10 |
| 2 | Understanding (Hiểu) | -1.10 | -0.41 |
| 3 | Applying (Vận dụng) | -0.41 | 0.20 |
| 4 | Analyzing (Phân tích) | 0.20 | 0.85 |
| 5 | Evaluating (Đánh giá) | 0.85 | 1.73 |
| 6 | Creating (Sáng tạo) | 1.73 | 3.00 |

### Phân bổ câu hỏi thích ứng (Fisher Information Weighting)

Khi tạo đề cá nhân hóa (Level 2 - Adaptive), hệ thống phân bổ số câu hỏi cho từng cấp Bloom dựa trên **Fisher Information** — đo lường cấp độ nào mang lại nhiều thông tin nhất về năng lực hiện tại của người dùng:

```
Fisher(θ, b) = P(θ, b) × (1 − P(θ, b))
```

Ví dụ: Người dùng ở θ = 0.5 (Applying/Analyzing) → hệ thống tự động phân bổ nhiều câu hơn ở level 3–4, ít câu hơn ở level 1–2 và 5–6.

Công thức phân bổ hỗn hợp (90% adaptive + 10% đồng đều) tránh bỏ sót hoàn toàn bất kỳ cấp độ nào.

### Hệ thống điểm ELO

Điểm ELO (0–1200) được ánh xạ tuyến tính từ Theta, với mỗi cấp Bloom đóng góp **200 điểm**:

```
ELO = levelIdx × 200 + (θ − levelMin) / levelSpan × 200
```

| ELO | Tương ứng |
|---|---|
| 0–200 | Remembering |
| 200–400 | Understanding |
| 400–600 | Applying |
| 600–800 | Analyzing |
| 800–1000 | Evaluating |
| 1000–1200 | Creating |

### Hiệu chỉnh độ khó câu hỏi (Online Recalibration)

Sau mỗi lần nộp bài, độ khó `b` của từng câu hỏi trong ngân hàng được tự động hiệu chỉnh theo **Stochastic Gradient Descent**:

```
b_new = b_old + learningRate × (P(θ, b_old) − score)
```

- Nếu người dùng làm **sai** một câu dự đoán là dễ → `b` tăng lên (câu thực sự khó hơn)
- Nếu người dùng làm **đúng** một câu dự đoán là khó → `b` giảm xuống (câu thực sự dễ hơn)

Kết quả được giới hạn trong `[-3.0, +3.0]` và ghi hàng loạt vào MongoDB bằng `BulkOperations`.

### Lịch sử & giới hạn

- Lịch sử câu trả lời (dùng để MAP estimation) được giới hạn tối đa **100 phiên × session_size** câu
- Lịch sử Theta snapshots lưu tối đa **100 điểm** để vẽ biểu đồ tiến trình
- `highestElo` được theo dõi riêng biệt để gamification

> **Lộ trình nâng cấp:** Hệ thống hiện dùng 1PL (1 tham số) để đảm bảo hội tụ với dữ liệu thưa. Khi đủ dữ liệu (> 10,000 tương tác/câu hỏi), sẽ nâng cấp lên mô hình **2PL/3PL** với tham số Discrimination (`a`) và Guessing (`c`).

---

## 🛠 Công nghệ sử dụng

### Core

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| Java | 17 | Ngôn ngữ chính |
| Spring Boot | 3.5.15 | Framework backend |
| Maven | — | Build & dependency management |
| Lombok | 1.18.42 | Giảm boilerplate code |
| MapStruct | 1.6.3 | Mapping DTO ↔ Entity |

### Cơ sở dữ liệu

| Công nghệ | Vai trò |
|---|---|
| MySQL 8 | Lưu trữ dữ liệu người dùng, phân quyền |
| MongoDB | Lưu trữ bộ đề thi (document-based) |
| Redis | Cache, quản lý OTP, JWT blacklist |

### AI & Xử lý tài liệu

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| Google GenAI SDK | 1.2.0 | Gọi Gemini API sinh câu hỏi |
| Tess4J (Tesseract) | 5.15.0 | OCR — nhận dạng văn bản từ ảnh |
| Apache PDFBox | 2.0.29 | Phân tích & trích xuất nội dung PDF |
| LangChain4j | 1.10.0 | Framework AI/LLM integration |
| all-MiniLM-L6-v2 | 1.10.0-beta18 | Embedding model cho semantic search |

### Bảo mật & Auth

| Công nghệ | Vai trò |
|---|---|
| Spring Security | Cơ chế bảo vệ endpoint |
| Nimbus JOSE JWT | Tạo & xác thực JWT token |
| Spring OAuth2 Resource Server | Xác thực token trên API |
| Spring OAuth2 Client | Hỗ trợ Google OAuth2 login |
| Google API Client | Xác minh Google ID Token |

### Xuất file & Lưu trữ

| Công nghệ | Vai trò |
|---|---|
| Apache POI 5.4 | Tạo file .docx (Word) |
| iText7 / iTextPDF 5 | Tạo file .pdf |
| Cloudinary | Lưu trữ & phân phối file trên cloud |
| XDocReport + FreeMarker | Template engine cho .docx |
| docx4j | Chuyển đổi & export DOCX |

### DevOps & Infra

| Công nghệ | Vai trò |
|---|---|
| Docker | Container hóa ứng dụng |
| Kubernetes | Orchestration & scaling |
| Spring Boot Actuator | Health check & monitoring |
| SpringDoc OpenAPI 2.8 | Swagger UI tự động |
| spring-dotenv | Load biến môi trường từ .env |

---

## 🚀 Cài đặt & Chạy local

### Yêu cầu tiên quyết

- **JDK 17+**
- **Maven 3.8+** (hoặc dùng Maven Wrapper có sẵn)
- **MySQL 8**, **MongoDB**, **Redis** đang chạy
- **API Keys**: Google Gemini, Cloudinary

### Các bước cài đặt

**1. Clone repository:**

```bash
git clone https://github.com/wadiobao/demo-proccessing.git
cd demo-proccessing
```

**2. Cấu hình môi trường:**

```bash
cp .env.example .env
```

Mở file .env và điền các giá trị thực tế (xem phần [Cấu hình môi trường](#-cấu-hình-môi-trường)).

**3. Build dự án:**

```bash
# Linux / macOS
./mvnw clean install -DskipTests

# Windows
mvnw.cmd clean install -DskipTests
```

**4. Chạy ứng dụng:**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Ứng dụng chạy tại: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

---

## 🐳 Triển khai với Docker

**Build image:**

```bash
docker build -t ai-quiz-generator:latest .
```

**Chạy container:**

```bash
docker run -d \
  --name ai-quiz-generator \
  -p 8080:8080 \
  --env-file .env \
  ai-quiz-generator:latest
```

---

## ☸️ Triển khai với Kubernetes

Các file manifest Kubernetes nằm trong thư mục k8s/. Sử dụng file .example.yaml làm template:

```bash
# Sao chép các file template
cp k8s/secret.example.yaml     k8s/secret.yaml
cp k8s/deployment.example.yaml k8s/deployment.yaml
cp k8s/service.example.yaml    k8s/service.yaml
cp k8s/ingress.example.yaml    k8s/ingress.yaml

# Chỉnh sửa giá trị trong secret.yaml, sau đó apply
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

---

## ⚙️ Cấu hình môi trường

Tạo file .env từ .env.example:

| Biến | Mô tả | Ví dụ |
|---|---|---|
| GEMINI_API_KEY | API key Gemini AI | AIza... |
| DB_URL | JDBC URL cho MySQL | jdbc:mysql://localhost:3306/mydb |
| DB_USERNAME / DB_PASSWORD | Thông tin MySQL | root / password |
| MONGODB_URI | Connection string MongoDB | mongodb://localhost:27017/pdfstore |
| REDIS_HOST / REDIS_PORT | Kết nối Redis | localhost / 6379 |
| REDIS_PASSWORD | Mật khẩu Redis | — |
| CLOUDINARY_CLOUD_NAME | Tên cloud Cloudinary | my-cloud |
| CLOUDINARY_API_KEY / CLOUDINARY_API_SECRET | Thông tin Cloudinary | — |
| MAIL_USERNAME / MAIL_PASSWORD | SMTP Gmail | app@gmail.com / app password |
| JWT_SECRET_KEY | Secret key ký JWT (≥ 32 ký tự) | random string |
| CORS_ALLOWED_ORIGINS | Domain frontend được phép | http://localhost:3000 |
| TOKEN_ACCESS_TIME | Thời gian sống access token (giây) | 100000 |
| TOKEN_REFRESH_TIME | Thời gian sống refresh token (giây) | 259200 |

---

## 📡 API Reference

Toàn bộ API có thể khám phá trực tiếp tại **Swagger UI**: http://localhost:8080/swagger-ui.html

### 🔐 Authentication

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | /auth/login | Đăng nhập, nhận JWT access & refresh token | ❌ Public |
| POST | /auth/refresh | Làm mới access token bằng refresh token | ❌ Public |
| POST | /auth/google | Đăng nhập bằng Google ID Token | ❌ Public |
| POST | /auth/logout | Đăng xuất, vô hiệu hóa token | ✅ Required |

### 👤 User

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | /user/register | Bắt đầu đăng ký, gửi OTP về email | ❌ Public |
| POST | /user/register/otp | Xác thực OTP để hoàn tất đăng ký | ❌ Public |
| GET | /user/me | Lấy thông tin người dùng hiện tại | ✅ Required |

### 📄 PDF Processing

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | /api/handlepdf | Tạo đề thi từ PDF (công khai) | ❌ Public |
| POST | /api/handlepdf/private | Tạo đề thi từ PDF (lưu vào tài khoản) | ✅ Required |

### 📝 Quiz

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | /quiz | Lấy danh sách đề thi của người dùng | ✅ Required |
| GET | /quiz/{id} | Lấy chi tiết một bộ đề thi | ✅ Required |
| DELETE | /quiz/{id} | Xóa một bộ đề thi | ✅ Required |

---

## 📁 Cấu trúc dự án

```
demo-proccessing/
├── src/main/java/com/example/demo/
│   ├── modules/
│   │   ├── community/    # Module cộng đồng / chia sẻ đề thi
│   │   ├── document/     # Module xử lý PDF, OCR, xuất file
│   │   ├── identity/     # Module xác thực, người dùng, JWT
│   │   └── quiz/         # Module quản lý đề thi
│   ├── common/           # Shared utilities, base classes
│   ├── config/           # Spring configuration (Security, Redis, ...)
│   ├── constants/        # Hằng số toàn cục
│   ├── dto/              # Data Transfer Objects
│   ├── enums/            # Enum definitions
│   ├── exception/        # Global exception handling
│   ├── utils/            # Helper utilities
│   └── validation/       # Custom validators
├── k8s/                  # Kubernetes manifests
├── tessdata/             # Tesseract OCR language data
├── Dockerfile            # Multi-stage Docker build
├── pom.xml               # Maven dependencies
└── .env.example          # Template biến môi trường
```
