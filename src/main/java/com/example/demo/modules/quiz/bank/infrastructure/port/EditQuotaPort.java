package com.example.demo.modules.quiz.bank.infrastructure.port;

/**
 * Port for managing the user's daily edit quota for the Question Bank.
 */
public interface EditQuotaPort {

    /**
     * Checks if the user has reached their daily limit.
     */
    boolean hasExceededQuota(String username);

    /**
     * Increments the user's edit count for the current day.
     */
    void incrementQuota(String username);

    /**
     * Gets the current edit count for the user.
     */
    int getCurrentCount(String username);
}
