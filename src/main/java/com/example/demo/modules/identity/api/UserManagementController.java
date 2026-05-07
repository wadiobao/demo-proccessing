package com.example.demo.modules.identity.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.identity.domain.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final IdentityFacade identityFacade;

    @GetMapping
    public ResponseEntity<StateResponse<Page<User>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(StateResponse.<Page<User>>builder()
                .result(identityFacade.getAllUsers(pageable))
                .build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<StateResponse<Void>> deleteUser(@PathVariable Long userId) {
        identityFacade.deleteUser(userId);
        return ResponseEntity.ok(StateResponse.<Void>builder().build());
    }
}
