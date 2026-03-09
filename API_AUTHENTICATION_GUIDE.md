# API Authentication Guide

This document lists the API endpoints and whether they require an authentication token (JWT).

## 🟢 Public Endpoints (No Token Required)

These endpoints are accessible without any authentication.

### Authentication & Users
- `POST /api/v1/user/register`: Register a new account.
- `POST /api/v1/user/register/otp`: Verify OTP for registration.
- `POST /api/v1/auth/login`: Log in to get access and refresh tokens.
- `POST /api/v1/auth/logout`: Log out (clears cookies).
- `POST /api/v1/auth/refresh`: Refresh access token using refresh token.
- `GET /api/v1/auth/introspect`: Check token validity.
- `POST /api/v1/user/forgot-password`: Send OTP for password recovery.
- `POST /api/v1/user/reset-password`: Reset password using OTP.

### Quiz & Processing
- `POST /api/v1/quiz/public`: Generate a quiz without saving data.
- `GET /api/v1/pdfs/**`: Access/Download PDF files.
- `POST /api/webhook/cloudinary`: Cloudinary webhook (System use).

### Discussion & Social (Read-only)
- `GET /api/v1/discussion/**`: View Topics, Forms, and Comments.
- `GET/POST /api/v1/mail/donate`: Donation-related emails.
- `GET/POST /api/v1/mail/send-bug`: Bug reporting.

---

## 🔴 Private Endpoints (Token Required)

These endpoints require a valid `access-token` cookie or `Authorization: Bearer <token>` header.

### Authenticated Quiz Features
- `POST /api/v1/quiz/private`: Generate a quiz and save it to your account.
- `POST /api/v1/quiz/submit`: Submit answers for scoring and IRT calculation.
- `GET /api/v1/quiz/stats`: View your quiz statistics for a specific topic.
- `GET /api/v1/quiz/stats/overview`: View overall learning performance.

### User Profile & Security
- `GET /api/v1/user/{username}`: View specific user information.
- `GET /api/v1/user/myinfor`: View your own profile and reputation.
- `PUT /api/v1/user/profile`: Update user avatar (multipart/form-data).
- `PUT /api/v1/user/change-password`: Change password for authenticated user.

### Community Contribution & Feedback
- `POST /api/v1/discussion/{formId}/vote`: Vote up/down on a post.
- `POST /api/v1/discussion/upload-questions`: AI-driven question extraction and bulk upload.
- `POST /api/v1/discussion/{topicId}/newform`: Create a new discussion post.

### Data Storage & Archive
- `POST /api/v1/mongo`: Create archived record.
- `GET /api/v1/mongo/author`: Get quizzes by author (Checks ownership or Admin).
- `GET /api/v1/mongo/all`: List all archived questions.

---

## 🛡️ Tier-Restricted Endpoints

These endpoints have additional logic-based requirements.

### Expert/Moderator Features
Requires `EXPERT`, `MODERATOR`, or `ADMIN` tier (High Reputation).

- `PUT /api/v1/question-bank/{id}`: Edit a curated question in the community bank.

### Admin Only
Requires `ROLE_ADMIN` authority.

- `GET /api/v1/user`: List all registered users (Paginated).
