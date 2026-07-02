package com.example.demo.modules.quiz.evaluation.application.service.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.demo.modules.quiz.evaluation.application.service.IRTCalculator;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.extern.slf4j.Slf4j;

/**
 * Simulator for testing the Item Response Theory (IRT) evaluation and adaptive question selection.
 *
 * <p>[EN] This simulator executes a multi-session testing scenario to demonstrate how the system mathematically 
 * models a user's latent ability (Theta) over time. It provides a controllable environment to observe the 
 * convergence of the estimated ability toward a hypothetical true ability, validating the accuracy of the IRT algorithm.
 * 
 * <p>[VI] Lớp giả lập môi trường thi trắc nghiệm thích ứng để kiểm thử thuật toán IRT. Lớp này thực thi một kịch bản
 * người dùng làm bài qua nhiều phiên, giúp trực quan hóa quá trình điểm năng lực (Theta) hội tụ dần về mức 
 * năng lực thực sự giả định, qua đó xác thực độ chính xác của các thuật toán đánh giá.
 *
 * @since 1.0
 */
@Slf4j
public class IrtUserSimulator {

    public static void main(String[] args) {
        IRTCalculator calculator = new IRTCalculator();
        Random random = new Random();        // [EN] Simulation configuration parameters
        // [VI] Thông số cấu hình mô phỏng
        int numSessions = 5;            // [EN] Number of sessions (n) / [VI] Số lượng phiên (n)
        int questionsPerSession = 20;   // [EN] Questions per session / [VI] Số câu hỏi trong mỗi phiên
        
        System.out.println("=== IRT User Multi-Session Simulator ===");
        
        // [EN] Initial theta for a new user
        // [VI] Theta khởi tạo cho người dùng mới
        double currentTheta = 0.0;
        System.out.printf("Initial System Theta: %.4f%n", currentTheta);

        // [EN] Parameters for MAP estimation
        // [VI] Các tham số cho thuật toán ước lượng MAP
        double sigma = 1.0;
        double dampingFactor = 0.5;

        // [EN] This is the hypothetical TRUE ability of the user (between -3.0 and 3.0).
        // [VI] Đây là năng lực THỰC SỰ giả định của người dùng (nằm trong khoảng -3.0 đến 3.0).
        double trueAbility = 1.5; 
        System.out.printf("User's True Ability (Hidden): %.4f%n", trueAbility);
        System.out.println("--------------------------------------------------------------------------------");

        // [EN] Accumulative history of the user across all sessions
        // [VI] Lịch sử toàn bộ câu trả lời của người dùng qua các phiên
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

                // [EN] Adaptive logic: select question difficulty close to current estimated theta
                // [VI] Logic thích ứng: Gợi ý câu hỏi có độ khó bám sát với điểm Theta hiện tại
                double pTarget = 0.8; // Trùng với pTarget = 0.8 trong hàm reviewAnswer thực tế
                double suggestedB = calculator.suggestDifficultyB(currentTheta, pTarget);
                
                // [EN] Add slight randomness to suggested difficulty
                // [VI] Thêm một chút ngẫu nhiên vào độ khó được gợi ý
                double b = suggestedB + (random.nextDouble() * 0.4 - 0.2);
                b = Math.max(-3.0, Math.min(3.0, b));
                
                // [EN] Calculate true probability based on hidden real ability
                // [VI] Tính xác suất trả lời đúng thực tế dựa trên năng lực thật sự (ẩn)
                double trueProbability = calculator.p(trueAbility, b);
                
                // [EN] Simulate answering
                // [VI] Giả lập việc người dùng trả lời câu hỏi
                boolean isCorrect = random.nextDouble() < trueProbability;
                if (isCorrect) {
                    sessionCorrectCount++;
                    totalCorrectCount++;
                }
                
                // [EN] Randomly select a Bloom level for the question
                // [VI] Chọn ngẫu nhiên một cấp độ Bloom cho câu hỏi
                int bloomIndex = random.nextInt(IRTCalculator.UserAnswerGenerator.BLOOM_LEVELS.length);
                String bloomLevel = IRTCalculator.UserAnswerGenerator.BLOOM_LEVELS[bloomIndex];
                globalBloomCounts[bloomIndex]++;
                
                UserAnswer answer = UserAnswer.builder()
                        .id("Q" + globalQuestionNumber)
                        .difficulty(b)
                        .correct(isCorrect)
                        .bloomLevel(bloomLevel)
                        .build();
                
                // [EN] Accumulate history across all sessions for accurate MAP estimation
                // [VI] Cộng dồn lịch sử qua các phiên để tính toán MAP chính xác nhất
                globalHistory.add(answer);

                // [EN] Re-estimate theta with the accumulating history
                // [VI] Tính toán lại điểm Theta với lịch sử đã được cộng dồn
                double newTheta = calculator.estimateThetaMAP(globalHistory, currentTheta, sigma, dampingFactor);
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
            System.out.printf("- %-15s: %d%n", IRTCalculator.UserAnswerGenerator.BLOOM_LEVELS[i], globalBloomCounts[i]);
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Final Estimated Theta after %d sessions: %.4f%n", numSessions, currentTheta);
        System.out.printf("Final Mastery Level: %d (%s)%n", 
                calculator.calculateMasteryLevel(currentTheta),
                calculator.mapToBloom(currentTheta));
                
        // [EN] Convergence check
        // [VI] Kiểm tra mức độ hội tụ (Sai số)
        double error = Math.abs(trueAbility - currentTheta);
        System.out.printf("Estimation Error (Difference from True Ability %.4f): %.4f%n", trueAbility, error);
        System.out.println("================================================================================");
        }
    

}
