package com.example.demo.modules.identity.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import com.example.demo.modules.identity.domain.model.AuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL; // Mặc định là LOCAL cho user cũ

    @Column(name = "provider_id", nullable = true, unique = true)
    private String providerId; // Sẽ lưu chuỗi 'sub' (Google User ID)
}
