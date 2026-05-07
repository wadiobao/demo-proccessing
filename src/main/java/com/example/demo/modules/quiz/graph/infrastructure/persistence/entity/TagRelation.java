package com.example.demo.modules.quiz.graph.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Edge in the Co-occurrence Tag Graph.
 * 
 * <p>Represents the relational weight (co-occurrence frequency) between 
 * two literal string tags, optimized to bypass JPA JOINS for fast hops.
 * 
 * @since 1.2
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tag_relations", indexes = {
    // Unique pair to prevent duplication 
    @Index(name = "idx_tag1_tag2", columnList = "tag1_name, tag2_name", unique = true),
    // Fast sort index
    @Index(name = "idx_weight", columnList = "weight_score")
})
public class TagRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // We store the literal tag name (lowercased) here so we don't have to JOIN 'Tag' table when doing Mongo tag intersections!
    @Column(name = "tag1_name", nullable = false, length = 100)
    String tag1Name;

    @Column(name = "tag2_name", nullable = false, length = 100)
    String tag2Name;

    @Builder.Default
    @Column(name = "weight_score", nullable = false)
    Long weightScore = 1L;
}
