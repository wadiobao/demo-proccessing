package com.example.demo.modules.quiz.bank.infrastructure.port;

/**
 * Port for verifying if a contributor has enough reputation to edit the Question Bank.
 */
public interface AuthorReputationPort {
    
    /**
     * Checks if the user is an expert or moderator (or admin).
     */
    boolean isAuthorizedToEdit(String username);
}
