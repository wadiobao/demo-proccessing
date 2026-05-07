package com.example.demo.modules.identity.domain.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    private String name;
    private String description;
    private Set<Permission> permissions;
}
