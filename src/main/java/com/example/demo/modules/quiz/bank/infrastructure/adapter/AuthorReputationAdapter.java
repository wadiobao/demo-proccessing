package com.example.demo.modules.quiz.bank.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.bank.infrastructure.port.AuthorReputationPort;
import com.example.demo.modules.identity.domain.model.Admin;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for verifying user reputation using SQL entities.
 */
@Component
@RequiredArgsConstructor
public class AuthorReputationAdapter implements AuthorReputationPort {

    private final IUserRepository userRepository;

    @Override
    public boolean isAuthorizedToEdit(String username) {
        User user = userRepository.findByUserName(username)
                .orElse(null);

        if (user == null) {
            return false;
        }

        // Admin is always authorized
        if (user instanceof Admin) {
            return true;
        }

        // NormalUser must be EXPERT or MODERATOR
        if (user instanceof NormalUser normalUser) {
            Tier currentTier = normalUser.getCurrentTier();
            if (currentTier == null) {
                return false;
            }
            
            String tierId = currentTier.getId();
            return "EXPERT".equals(tierId) || "MODERATOR".equals(tierId);
        }

        return false;
    }
}
