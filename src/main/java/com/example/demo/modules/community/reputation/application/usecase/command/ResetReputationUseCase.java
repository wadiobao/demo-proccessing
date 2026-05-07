package com.example.demo.modules.community.reputation.application.usecase.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.repository.ITierRepository;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetReputationUseCase {

    private final IUserRepository userRepository;
    private final ITierRepository tierRepository;

    @Transactional
    public void execute() {
        log.info("Starting monthly reputation reset...");
        Tier contributorTier = tierRepository.findById("CONTRIBUTOR").orElse(null);
        
        userRepository.findAll().forEach(user -> {
            if (user instanceof NormalUser normalUser && normalUser.getReputationScore() < 0) {
                normalUser.setReputationScore(0);
                normalUser.setCurrentTier(contributorTier);
                normalUser.setLastReputationReset(LocalDateTime.now());
                userRepository.save(normalUser);
            }
        });
    }
}
