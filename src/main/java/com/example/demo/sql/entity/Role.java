package com.example.demo.sql.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role defines a set of permissions that can be assigned to a user.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private String name; // e.g., "ADMIN", "USER"

    private String description;

    @ManyToMany
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Set<Permission> permissions;
}
