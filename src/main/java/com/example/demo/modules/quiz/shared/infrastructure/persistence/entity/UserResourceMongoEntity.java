package com.example.demo.modules.quiz.shared.infrastructure.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.modules.quiz.shared.domain.model.ThetaSnapshot;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * MongoDB Entity for storing user learning progress and IRT parameters.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_resource")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResourceMongoEntity extends BaseModel {
    @Id
    String id;
    
    // topic is unique within context
    String topic;
    
    @Default
    List<String> contentIds = new ArrayList<>();

    @Indexed
    String userName;

    @Default
    List<UserAnswer> history = new ArrayList<>();
    
    @Default
    double theta = 0.0;
    
    @Default
    double b = 0.0;
    
    @Default
    int mastery = 1;

    /** ELO score (0–1200) derived from theta after each session. / Điểm ELO (0–1200) tính từ theta sau mỗi phiên. */
    @Default
    int elo = 0;
    
    @Default
    int highestElo = 0;

    /**
     * Fixed number of questions per quiz session for this topic.
     * Chosen by the user at topic creation and kept constant throughout the learning journey.
     * Supported values: 15, 30, 50.
     */
    @Default
    int sessionSize = 15;

    /**
     * Chronological record of theta scores after each quiz session.
     * Capped at 100 entries to prevent unbounded document growth.
     */
    @Default
    List<ThetaSnapshot> thetaHistory = new ArrayList<>();

    @Version
    Long version;
}
