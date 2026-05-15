package com.example.demo.modules.quiz.adaptive.api.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.modules.quiz.shared.domain.model.ThetaSnapshot;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Unified response for the topic overview endpoint.
 *
 * <p>Merges data previously spread across {@code /topics/score-history} and
 * {@code /topics/files} into a single payload, eliminating the double round-trip
 * cost on the client side.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicOverviewResponse {

    /** Topic identifier / ID của chủ đề. */
    String id;

    /** Topic name / Tên chủ đề. */
    String topic;

    /** Creation timestamp / Thời điểm tạo. */
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDateTime createdAt;

    /** Number of questions per quiz session / Số câu hỏi mỗi phiên. */
    int sessionSize;

    // ── IRT / ELO ─────────────────────────────────────────────

    /** Raw IRT ability estimate / Ước lượng năng lực IRT thô. */
    double theta;

    /** Discrete Bloom mastery level (1–6) / Cấp độ thành thạo Bloom rời rạc. */
    int mastery;

    /** Human-readable Bloom mastery label / Nhãn cấp độ thành thạo. */
    String masteryLabel;

    /**
     * Total ELO score across all Bloom levels (0–1200).
     * 0 for legacy documents that pre-date ELO tracking.
     */
    int elo;

    /** ELO points remaining to reach the next mastery level / ELO còn thiếu để lên cấp. */
    int eloToNextLevel;
    
    /** The highest elo in history / ELO cao nhất trong lịch sử*/
    int highestElo;

    // ── History ───────────────────────────────────────────────

    /** Chronological record of theta scores after each session / Lịch sử điểm theta theo phiên. */
    List<ThetaSnapshot> thetaHistory;

    // ── Files ─────────────────────────────────────────────────

    /**
     * Files attached to this topic.
     * Fetched in a single batch call (findByIds) to avoid N+1.
     */
    List<TopicFileResponse> files;
}
