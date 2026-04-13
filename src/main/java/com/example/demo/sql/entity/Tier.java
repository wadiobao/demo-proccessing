package com.example.demo.sql.entity;
 
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
public class Tier {
 
    @Id
    String id; // e.g., "RESTRICTED", "CONTRIBUTOR", "MEMBER", "EXPERT", "MODERATOR"
 
    String description;
 
    @Builder.Default
    int minReputation = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Permission> permissions;
}
