package com.example.demo.modules.quiz.adaptive.application.query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.enums.TimeRange;
import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.shared.domain.model.ThetaSnapshot;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.UserResourceMongoEntity;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.repository.UserResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Query for retrieving historical mastery scores for a specific learning topic.
 * Only reads system state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTopicScoreHistoryQuery {

    private final UserResourceRepository userResourceRepository;
    private final IRTCalculator irtCalculator;

    public StateResponse<Object> execute(String topicId, String username) {
        log.info("Executing get all topic score history ranges query for user: {}, topic: {}", username, topicId);
        
        UserResourceMongoEntity userResource = userResourceRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!userResource.getUserName().equals(username)) {
            throw new SecurityException("You do not have permission to view this topic");
        }
        
        List<ThetaSnapshot> fullHistory = userResource.getThetaHistory();
        Map<TimeRange, List<ThetaSnapshot>> results = new EnumMap<>(TimeRange.class);
        
        for (TimeRange range : TimeRange.values()) {
            results.put(range, filterHistoryByRange(fullHistory, range));
        }

        return StateResponse.builder()
                .message("Topic score history for all ranges retrieved successfully")
                .result(results)
                .build();
    }

    private List<ThetaSnapshot> filterHistoryByRange(List<ThetaSnapshot> history, TimeRange range) {
        if (history == null) return List.of();
        
        LocalDate today = LocalDate.now();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);
        LocalDateTime startDateTime;

        switch (range) {
            case TODAY:
                startDateTime = today.atStartOfDay();
                break;
            case WEEK:
                startDateTime = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                endDateTime = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                break;
            case ONE_MONTH:
                startDateTime = today.minusMonths(1).atStartOfDay();
                break;
            case THREE_MONTHS:
                startDateTime = today.minusMonths(3).atStartOfDay();
                break;
            case SIX_MONTHS:
                startDateTime = today.minusMonths(6).atStartOfDay();
                break;
            case ONE_YEAR:
                startDateTime = today.minusYears(1).atStartOfDay();
                break;
            default:
                return history;
        }

        final LocalDateTime finalStart = startDateTime;
        final LocalDateTime finalEnd = endDateTime;

        return history.stream()
                .filter(t -> {
                    LocalDateTime time = t.getRecordedAt();
                    return time != null && !time.isBefore(finalStart) && !time.isAfter(finalEnd);
                })
                .collect(Collectors.toList());
    }
}