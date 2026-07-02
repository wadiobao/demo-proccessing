package com.example.demo.modules.quiz.evaluation.application.service.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.demo.modules.quiz.evaluation.application.service.irt.AdaptiveScheduler;
import com.example.demo.modules.quiz.evaluation.application.service.irt.BloomMasteryMapper;
import com.example.demo.modules.quiz.evaluation.application.service.irt.IRTEngine;
import com.example.demo.modules.quiz.evaluation.application.service.irt.UserAnswerGenerator;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.extern.slf4j.Slf4j;

/**
 * Simulator for testing the Item Response Theory (IRT) evaluation and adaptive question selection.
 *
 * <p>[EN] This simulator executes a multi-session testing scenario using 1PL, 2PL, and 3PL parameters 
 * to demonstrate how the system mathematically models a user's latent ability (Theta) over time.
 * 
 * <p>[VI] Lớp giả lập môi trường thi trắc nghiệm thích ứng bằng cách mô phỏng tương tác 3PL (độ phân biệt, độ đoán mò)
 * để kiểm thử mức độ hội tụ của thuật toán IRT.
 */
@Slf4j
public class IrtUserSimulator {

    public static void main(String[] args) {
        IRTEngine engine = new IRTEngine();
        BloomMasteryMapper mapper = new BloomMasteryMapper();
        AdaptiveScheduler scheduler = new AdaptiveScheduler(engine);
        Random random = new Random();

        int numSessions = 5;
        int questionsPerSession = 20;
        
        System.out.println("=== IRT User Multi-Session Simulator (3PL Model Enabled) ===");
        
        double currentTheta = 0.0;
        System.out.printf("Initial System Theta: %.4f%n", currentTheta);

        double sigma = 1.0;
        double dampingFactor = 0.5;

        // True ability
        double trueAbility = 1.5; 
        System.out.printf("User's True Ability (Hidden): %.4f%n", trueAbility);
        System.out.println("--------------------------------------------------------------------------------");

        List<UserAnswer> globalHistory = new ArrayList<>();
        int totalCorrectCount = 0;
        int[] globalBloomCounts = new int[6];

        for (int session = 1; session <= numSessions; session++) {
            System.out.printf("\n>>> STARTING SESSION %d <<<%n", session);
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("%-5s | %-15s | %-10s | %-12s | %-10s | %-15s%n", 
                    "Q#", "Difficulty (b)", "Correct?", "P(correct)", "New Theta", "Delta");
            System.out.println("--------------------------------------------------------------------------------");

            int sessionCorrectCount = 0;

            for (int i = 1; i <= questionsPerSession; i++) {
                int globalQuestionNumber = (session - 1) * questionsPerSession + i;

                // Adaptive selection
                double pTarget = 0.8;
                double suggestedB = scheduler.suggestDifficultyB(currentTheta, pTarget);
                
                // Add noise to b
                double b = suggestedB + (random.nextDouble() * 0.4 - 0.2);
                b = Math.max(-3.0, Math.min(3.0, b));
                
                // 3PL Simulation parameters
                double a = 1.2; // discrimination
                double c = 0.25; // guessing (e.g. 4 options)
                
                // True probability under 3PL
                double trueProbability = engine.p3PL(trueAbility, b, a, c);
                
                // Simulate outcome
                boolean isCorrect = random.nextDouble() < trueProbability;
                if (isCorrect) {
                    sessionCorrectCount++;
                    totalCorrectCount++;
                }
                
                int bloomIndex = random.nextInt(UserAnswerGenerator.BLOOM_LEVELS.length);
                String bloomLevel = UserAnswerGenerator.BLOOM_LEVELS[bloomIndex];
                globalBloomCounts[bloomIndex]++;
                
                UserAnswer answer = UserAnswer.builder()
                        .id("Q" + globalQuestionNumber)
                        .difficulty(b)
                        .correct(isCorrect)
                        .bloomLevel(bloomLevel)
                        .build();
                
                globalHistory.add(answer);

                // Estimate Theta using 3PL
                double newTheta = engine.estimateThetaMAP3PL(globalHistory, currentTheta, sigma, dampingFactor, a, c);
                double delta = newTheta - currentTheta;
                
                System.out.printf("%-5d | %-15.4f | %-10b | %-12.2f | %-10.4f | %+-15.4f%n",
                        i, b, isCorrect, trueProbability, newTheta, delta);
                
                currentTheta = newTheta;
            }

            double sessionAccuracy = (double) sessionCorrectCount / questionsPerSession * 100;
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("Session %d Summary:%n", session);
            System.out.printf("- Correct Answers: %d/%d (%.2f%%)%n", sessionCorrectCount, questionsPerSession, sessionAccuracy);
            System.out.printf("- End of Session Theta: %.4f%n", currentTheta);
        }
        
        System.out.println("\n================================================================================");
        System.out.println("=== FINAL SIMULATION STATISTICS ===");
        System.out.printf("Total Sessions Simulated: %d%n", numSessions);
        System.out.printf("Total Questions Answered: %d%n", numSessions * questionsPerSession);
        
        double overallAccuracy = (double) totalCorrectCount / (numSessions * questionsPerSession) * 100;
        System.out.printf("Overall Correct Answers: %d (%.2f%%)%n", totalCorrectCount, overallAccuracy);
        System.out.println("\nQuestion Type Breakdown (Global):");
        for (int i = 0; i < globalBloomCounts.length; i++) {
            System.out.printf("- %-15s: %d%n", UserAnswerGenerator.BLOOM_LEVELS[i], globalBloomCounts[i]);
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Final Estimated Theta after %d sessions: %.4f%n", numSessions, currentTheta);
        System.out.printf("Final Mastery Level: %d (%s)%n", 
                mapper.calculateMasteryLevel(currentTheta),
                mapper.mapToBloom(currentTheta));
                
        double error = Math.abs(trueAbility - currentTheta);
        System.out.printf("Estimation Error (Difference from True Ability %.4f): %.4f%n", trueAbility, error);
        System.out.println("================================================================================");
    }
}
