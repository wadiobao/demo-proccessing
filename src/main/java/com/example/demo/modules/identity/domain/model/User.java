package com.example.demo.modules.identity.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private Date date;
    private String avatarUrl;
    private Role role;
}
