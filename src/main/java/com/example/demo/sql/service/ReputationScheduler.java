package com.example.demo.sql.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Background scheduler for community reputation and tier maintenance.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReputationScheduler {

    private final ReputationService reputationService;

    /**
     * Resets negative reputation scores to 0 on the 1st of every month at midnight.
     * Expression: "0 0 0 1 * ?" (Sec Min Hour Day Month Week)
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void monthlyReputationReset() {
        log.info("Cron job triggered: Monthly Reputation Reset");
        reputationService.performMonthlyReset();
    }
}
