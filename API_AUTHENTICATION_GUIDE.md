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

### Quiz & Processing
- `POST /api/v1/quiz/public`: Generate a quiz without saving data.
- `GET /api/v1/pdfs/**`: Access/Download PDF files.
- `POST /api/webhook/cloudinary`: Cloudinary webhook (System use).

### Discussion & Social
- `GET/POST /api/v1/discussion/**`: View and participate in discussions (Topics, Forms, Comments).
- `GET/POST /api/v1/mail/donate`: Donation-related emails.
- `GET/POST /api/v1/mail/send-bug`: Bug reporting.

---

## 🔴 Private Endpoints (Token Required)

These endpoints require a valid `access-token` cookie or `Authorization: Bearer <token>` header.

### Authenticated Quiz Features
- `POST /api/v1/quiz/private`: Generate a quiz and save it to your account.
- `POST /api/v1/quiz/submit`: Submit answers for scoring and IRT calculation.
- `GET /api/v1/quiz/stats`: View your quiz statistics/performance.

### User Management
- `GET /api/v1/user/{username}`: View specific user information.
- `GET /api/v1/user/myinfor`: View your own profile information.

### Data Storage (Archived)
- `POST /mongo`: Create archived record.
- `GET /mongo/author`: Get quizzes by author (Checks if you are the owner or admin).
- `GET /mongo/all`: List all archived questions.

---

## 🛡️ Restricted Endpoints (Admin Only)

Requires `ROLE_ADMIN` authority.

- `GET /api/v1/user`: List all registered users (Paginated).
