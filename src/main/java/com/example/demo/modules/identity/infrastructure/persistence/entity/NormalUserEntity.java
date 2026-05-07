package com.example.demo.modules.identity.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class NormalUserEntity extends UserEntity {

    @Builder.Default
    private int reputationScore = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    private TierEntity currentTier;

    private LocalDateTime lastReputationReset;
}
