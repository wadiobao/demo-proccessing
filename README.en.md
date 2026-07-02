# 📄 AI Quiz Generator — AI-Powered Quiz Generation System & Adaptive IRT Engine

> Automatically converts PDF documents into high-quality quiz banks using Google Gemini AI, OCR, and custom Adaptive Learning Engines.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [System Architecture](#-system-architecture)
- [Features](#-features)
- [Adaptive Learning IRT Engine](#-adaptive-learning-irt-engine)
- [Technology Stack](#-technology-stack)
- [Installation & Local Setup](#-installation--local-setup)
- [Docker Deployment](#-docker-deployment)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [Environment Configuration](#-environment-configuration)
- [API Reference](#-api-reference)
- [Project Structure](#-project-structure)

---

## 🧭 Overview

**AI Quiz Generator** is a robust Spring Boot 3 backend system designed to automate the process of creating academic and professional quizzes from uploaded documents. Users upload a PDF (scanned or native), the system extracts the text (using OCR if necessary), analyzes the content, and calls Google Gemini AI to construct appropriate question sets. These quizzes can then be exported to `.docx` or `.pdf` formats, and are automatically customized to the user's proficiency level over time.

---

## 🏗 System Architecture

```
Client (HTTP Request)
       │
       ▼
Spring Boot API (Port 8080)
       │
       ├── MySQL       — User accounts & Permission management
       ├── MongoDB     — Flexible quiz document storage (document-based)
       ├── Redis       — OTP cache, JWT blacklist, session management
       └── Cloudinary  — Cloud media and document storage
       │
       ├── Google Gemini AI   — Automatic question generation
       ├── Tess4J (Tesseract) — OCR for text extraction from images
       ├── Apache PDFBox      — PDF parsing & analysis
       └── IRT Engine (1PL/Rasch/3PL + MAP) — Latent ability evaluation & adaptive flow
```

---

## ✨ Features

| Feature | Description |
|---|---|
| 🤖 **AI Question Generation** | Generates quizzes automatically from PDF content using Google Gemini AI |
| 🔍 **OCR Support** | Optical Character Recognition (OCR) for scanned PDFs using Tesseract (Vietnamese supported) |
| 📝 **Flexible Export** | Exports quizzes to `.docx` (Word) or `.pdf` |
| 🔐 **Stateless Security** | Stateless authentication using JWT Access and Refresh tokens |
| 📧 **OTP Registration** | Email-based OTP for secure account registration |
| 🔑 **Google OAuth2** | Fast login using Google OAuth2 integration |
| 🧠 **Semantic Search** | Semantic search on questions using LangChain4j and all-MiniLM-L6-v2 Embeddings |
| 📊 **Swagger UI** | Interactive API documentation at `/swagger-ui.html` |
| 🎯 **Adaptive IRT Engine** | Evaluates actual user ability (Theta) and adapts quiz difficulty dynamically |
| 📈 **ELO Scoring** | Maps Theta to ELO score (0–1200), reflecting progress according to Bloom's Taxonomy |
| 🐳 **Docker & K8s Ready** | Pre-configured containerized environments ready for Kubernetes deployment |

---

## 🎯 Adaptive Learning IRT Engine

The system features a custom-built Item Response Theory (IRT) engine supporting **1-Parameter Logistic (1PL / Rasch Model)** and **3-Parameter Logistic (3PL)** frameworks combined with **Maximum A Posteriori (MAP) Estimation** to dynamically estimate and adapt to student performance.

### Mathematical Model

The probability $P$ of a correct response given user ability $\theta$, item difficulty $b$, discrimination $a$, and pseudo-guessing $c$ is:

```
P(correct | θ, b, a, c) = c + (1 - c) / (1 + e^(-a * (θ - b)))
```

| Symbol | Meaning | Domain |
|---|---|---|
| `θ` (Theta) | Latent student ability | `[-3.0, +3.0]` |
| `b` (Difficulty) | Question difficulty on the same scale | `[-3.0, +3.0]` |
| `a` (Discrimination) | Item discrimination power | `[0.0, 2.0]` (Default 1.0) |
| `c` (Guessing) | Pseudo-guessing probability | `[0.0, 1.0]` (Default 0.0) |

### Adaptive Workflow

```
[User Submits Answers]
          │
          ▼
1. Calculate Raw Score (correctAnswers / totalQuestions)
          │
          ▼
2. MAP Estimation (Newton-Raphson, up to 100 iterations)
   → Update θ based on response history
          │
          ▼
3. Recalibrate Item Difficulty (b) via SGD
   → Bulk write updates to MongoDB
          │
          ▼
4. Map θ to Mastery Level (1–6) & ELO (0–1200)
          │
          ▼
5. Persist ThetaSnapshot & Update User Resource
```

### Integration with Bloom's Taxonomy

The Theta scale is mapped to **6 cognitive levels of Bloom's Taxonomy** with uneven boundaries adjusted from empirical data:

| Level | Bloom Taxonomy Level | Theta Min | Theta Max | ELO Range |
|:---:|---|:---:|:---:|:---:|
| 1 | Remembering | -3.00 | -1.10 | 0 – 200 |
| 2 | Understanding | -1.10 | -0.41 | 200 – 400 |
| 3 | Applying | -0.41 | 0.20 | 400 – 600 |
| 4 | Analyzing | 0.20 | 0.85 | 600 – 800 |
| 5 | Evaluating | 0.85 | 1.73 | 800 – 1000 |
| 6 | Creating | 1.73 | 3.00 | 1000 – 1200 |

### Question Distribution (Fisher Information Weighting)

For adaptive tests (Level 2), the system allocates questions across Bloom levels using **Fisher Information**, which measures which levels yield the most information about the user's current ability $\theta$:

```
Fisher(θ, b) = P(θ, b) * (1 - P(θ, b))
```

A hybrid model allocation (90% adaptive + 10% uniform) ensures that no cognitive level is completely omitted.

---

## 🛠 Technology Stack

### Core & Database
- **Backend Framework**: Spring Boot 3.5.15
- **Programming Language**: Java 17
- **Relational DB**: MySQL 8 (For users, roles, and schema metadata)
- **Document Store**: MongoDB (For highly unstructured, flexible quiz sets)
- **Caching & Sessions**: Redis (OTP verification, JWT blacklists)
- **Object Mapper**: MapStruct 1.6.3

### AI & NLP
- **LLM Integration**: Google GenAI SDK 1.2.0 (Gemini 1.5/2.0 API)
- **Semantic Search**: LangChain4j 1.10.0 + all-MiniLM-L6-v2 Embedding Model
- **OCR Engine**: Tess4J (Tesseract OCR) 5.15.0
- **PDF Parser**: Apache PDFBox 2.0.29

### Security & Auth
- **Security Framework**: Spring Security
- **JWT Provider**: Nimbus JOSE JWT
- **OAuth2 Integration**: Spring OAuth2 Resource Server & Client
- **Google OAuth**: Google API Client

### Export & Storage
- **Word Document Export**: Apache POI 5.4
- **PDF Document Export**: iText7
- **Cloud Storage**: Cloudinary (Cloud storage and distribution)

---

## 🚀 Installation & Local Setup

### Prerequisites
- **JDK 17+**
- **Maven 3.8+** (or use the included wrapper `./mvnw`)
- Running instances of **MySQL**, **MongoDB**, and **Redis**
- **API Keys**: Google Gemini API key, Cloudinary Cloud credentials

### Local Execution

1. **Clone repository:**
   ```bash
   git clone https://github.com/wadiobao/demo-proccessing.git
   cd demo-proccessing
   ```

2. **Configure environment:**
   ```bash
   cp .env.example .env
   ```
   *Edit `.env` and fill in your credential values (Gemini Keys, DB credentials, SMTP properties, etc.).*

3. **Build project:**
   ```bash
   # Linux / macOS
   ./mvnw clean install -DskipTests

   # Windows
   mvnw.cmd clean install -DskipTests
   ```

4. **Run application:**
   ```bash
   # Linux / macOS
   ./mvnw spring-boot:run

   # Windows
   mvnw.cmd spring-boot:run
   ```
   - API Server URL: http://localhost:8080
   - Swagger Documentation: http://localhost:8080/swagger-ui.html

---

## 🐳 Docker Deployment

```bash
# Build Docker image
docker build -t ai-quiz-generator:latest .

# Run Container with environment variables
docker run -d \
  --name ai-quiz-generator \
  -p 8080:8080 \
  --env-file .env \
  ai-quiz-generator:latest
```

---

## ☸️ Kubernetes Deployment

```bash
# Copy template manifests
cp k8s/secret.example.yaml     k8s/secret.yaml
cp k8s/deployment.example.yaml k8s/deployment.yaml
cp k8s/service.example.yaml    k8s/service.yaml
cp k8s/ingress.example.yaml    k8s/ingress.yaml

# Apply to cluster
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

---

## ⚙️ Environment Configuration

| Key | Description | Example |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API key | `AIzaSy...` |
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/quizdb` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL authentication credentials | `root` / `password` |
| `MONGODB_URI` | MongoDB connection URI | `mongodb://localhost:27017/quizdb` |
| `REDIS_HOST` / `REDIS_PORT` | Redis server address | `localhost` / `6379` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary identification | `my-cloud-storage` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP Email credentials | `example@gmail.com` / `app-pass` |
| `JWT_SECRET_KEY` | HS256 Secret key (>=32 chars) | `mySuperSecretKeyForSigningJWTs123!` |

---

## 📡 API Reference

Explore all endpoints directly through the **Swagger UI**: http://localhost:8080/swagger-ui.html

### 🔐 Authentication
- `POST /auth/login` - Stateless credentials sign-in.
- `POST /auth/refresh` - Refresh access tokens.
- `POST /auth/google` - Sign in using Google OAuth ID tokens.
- `POST /auth/logout` - Invalidate active session tokens.

### 👤 User Profile
- `POST /user/register` - Begin registration and send email OTP.
- `POST /user/register/otp` - Verify email OTP to activate account.
- `GET /user/me` - Fetch details of currently logged-in user.

### 📄 PDF Processing
- `POST /api/handlepdf` - Public upload and prompt-based quiz creation.
- `POST /api/handlepdf/private` - Authenticated document-to-quiz generation.

### 📝 Quiz Management
- `GET /quiz` - List quizzes owned by the authenticated user.
- `GET /quiz/{id}` - Fetch details of a single quiz.
- `DELETE /quiz/{id}` - Remove a specific quiz.

---

## 📁 Project Structure

```
demo-proccessing/
├── src/main/java/com/example/demo/
│   ├── modules/
│   │   ├── community/    # Forums, share spaces, and collaborative work
│   │   ├── document/     # PDF parsing, OCR adapters, and Docx/Pdf exports
│   │   ├── identity/     # User register, security settings, JWT context
│   │   └── quiz/         # Core quiz entities, test handlers, and IRT Engine
│   ├── common/           # Shared utilities, base exception classes
│   ├── config/           # Spring core configurations (Security, Redis, OpenAPI)
│   ├── utils/            # General helpers (Cloudinary, Gemini, String handling)
├── k8s/                  # Kubernetes deployment blueprints
├── tessdata/             # Tesseract OCR offline language models (.traineddata)
├── Dockerfile            # Container definition
├── pom.xml               # Maven configuration
└── .env.example          # Sample environment template
```
