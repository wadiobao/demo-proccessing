package com.example.demo.sql.dto.form;

import java.util.List;
import com.example.demo.mongo.dto.question.Question;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Wrapper for temporary form creation state in Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FormSession {
    String sessionId;
    String ownerName;
    List<Question> questions;
    long createdAt; // Timestamp
}
