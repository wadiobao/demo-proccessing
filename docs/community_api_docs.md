# 📄 Community Features API Documentation (v2.0)

This document provides the technical specifications for the new reputation, voting, and bulk ingestion features for the frontend (FE) integration.

## 1. User Reputation & Tiers
User profile responses now include prestige metrics.

### [GET] /api/v1/user/myinfor
**Description**: Fetches the current user's profile and reputation stats.

**Response Body (UserResponse)**:
```json
{
  "userName": "johndoe",
  "email": "john@example.com",
  "reputationScore": 520, 
  "currentTier": "EXPERT",
  "role": "USER"
}
```
*   `reputationScore`: Integer tracking prestige (+5 for upvote, -2 for downvote).
*   `currentTier`: `RESTRICTED`, `CONTRIBUTOR`, `EXPERT`, `MODERATOR`.

---

## 2. Community Voting System
Allows users to influence the prestige of discussion authors.

### [POST] /api/v1/discussion/{formId}/vote
**URL Params**: `formId` (String)
**Query Params**: `value` (Integer) -> `1` (Upvote), `-1` (Downvote)
**Auth**: Bearer Token required.

**Example Request**:
`POST /api/v1/discussion/65e123abc/vote?value=1`

**Success Response**:
```json
{
  "message": "Vote recorded successfully",
  "success": true
}
```

---

## 3. Bulk Question Upload
Allows mass ingestion of questions via Excel files. Restricted to users with positive reputation.

### [POST] /api/v1/discussion/upload-questions
**Content-Type**: `multipart/form-data`
**Payload**:
*   `file`: `.xlsx` file (following the provided template).
*   `topic`: Target subject area (String).

**Excel Template Requirements**:
| Column A | Column B | Column C | Column D | Column E | Column F | Column G |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Question | Ans A | Ans B | Ans C | Ans D | Correct | Explanation |

**Success Response**:
```json
{
  "result": [...], 
  "message": "Bulk upload completed",
  "success": true
}
```
*Note: Users with `RESTRICTED` tier will receive a 403/500 error.*

---

## 4. Community Question Bank Editing
Allows high-prestige users to refine the curated bank.

### [PUT] /api/v1/question-bank/{id}
**Description**: Update question content or difficulty.
**Restriction**: `EXPERT`, `MODERATOR`, or `ADMIN` only.
**Quota**: Max 5 edits per user per day.

**Request Body (QuestionBank)**:
```json
{
  "questionData": {
    "question": "Updated content here...",
    "answer": {
      "A": "New Ans A",
      "correct": "A"
    }
  }
}
```

**Success Response**:
```json
{
  "result": { ... },
  "message": "Question updated successfully"
}
```

---

## 5. Security & Error Handling
*   **403 Forbidden**: Occurs when a `RESTRICTED` user attempts to upload or a non-`EXPERT` attempts to edit the bank.
*   **Quota Error**: Returns a clear message if the 5-edit daily limit is exceeded.
*   **Reputation Recovery**: Point scores are automatically reset every **1st of the month** for users with negative standings.
