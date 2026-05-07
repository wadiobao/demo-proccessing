package com.example.demo.modules.identity.infrastructure.persistence.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;

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
public class RoleEntity {

    @Id
    private String name; // e.g., "ADMIN", "USER"

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(
        name = "roles_permissions",
        joinColumns = @JoinColumn(name = "role_name"),
        inverseJoinColumns = @JoinColumn(name = "permissions_name")
    )
    private Set<PermissionEntity> permissions;
}
