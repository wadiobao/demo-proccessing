package com.example.demo.modules.identity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Admin extends User {
    private String rootToken;
    /** Identifies the administrator's HR record for audit purposes. */
    private String employeeId;
    private String department;
}
