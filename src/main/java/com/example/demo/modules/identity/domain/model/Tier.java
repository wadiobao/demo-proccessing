package com.example.demo.modules.identity.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tier {
    private String id;
    private String name;
    private int minReputation;
    private List<Permission> extraPermissions;
}
