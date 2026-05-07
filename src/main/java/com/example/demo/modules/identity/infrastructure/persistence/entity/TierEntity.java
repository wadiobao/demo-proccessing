package com.example.demo.modules.identity.infrastructure.persistence.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Tier represents a reputation level in the community.
 * It defines the name, description, and minimum reputation required.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tiers")
public class TierEntity {

    @Id
    String id; // e.g., "RESTRICTED", "CONTRIBUTOR", "MEMBER", "EXPERT", "MODERATOR"

    String description;

    @Builder.Default
    int minReputation = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tiers_permissions",
        joinColumns = @JoinColumn(name = "tier_id"),
        inverseJoinColumns = @JoinColumn(name = "permissions_name")
    )
    Set<PermissionEntity> permissions;
}
