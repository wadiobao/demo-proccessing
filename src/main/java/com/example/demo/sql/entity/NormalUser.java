package com.example.demo.sql.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * NormalUser represents a regular student or user of the platform.
 * It contains specific attributes like reputation score and learning tier.
 *
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "normal_user_data")
public class NormalUser extends User {

    @Builder.Default
    private int reputationScore = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    private Tier currentTier;

    private LocalDateTime lastReputationReset;
}
