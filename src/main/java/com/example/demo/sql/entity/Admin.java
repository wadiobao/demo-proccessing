package com.example.demo.sql.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Admin represents a system administrator with elevated privileges.
 *
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "admin_data")
public class Admin extends User {

    private String employeeId; // Example domain-specific field for admins
    private String department;
}
