package com.example.demo.modules.community.reputation.application.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.modules.community.reputation.application.usecase.command.ResetReputationUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lập lịch tự động reset danh tiếng tiêu cực vào mỗi đầu tháng.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReputationScheduler {

    private final ResetReputationUseCase resetReputationUseCase;

    /**
     * Chạy vào lúc 00:00 ngày mùng 1 hàng tháng.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleMonthlyReset() {
        log.info("Cron job triggered: Monthly Reputation Reset");
        resetReputationUseCase.execute();
    }
}
