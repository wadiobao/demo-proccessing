package com.example.demo.modules.quiz.evaluation.application.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.evaluation.application.service.irt.AdaptiveScheduler;
import com.example.demo.modules.quiz.evaluation.application.service.irt.BloomMasteryMapper;
import com.example.demo.modules.quiz.evaluation.application.service.irt.IRTEngine;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mathematical engine for estimating user ability and adjusting question
 * difficulty in adaptive assessments.
 *
 * <p>
 * [EN] This class implements the Maximum A Posteriori (MAP) estimation using
 * the 1-Parameter Logistic (1PL), 2-Parameter Logistic (2PL), and 3-Parameter
 * Logistic (3PL) Item Response Theory (IRT) models.
 * <p>
 * [VI] Lớp thực thi thuật toán Cảm ứng Câu hỏi (IRT) sử dụng Mô hình Logistic
 * 1PL, 2PL, và 3PL.
 *
 * @since 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IRTCalculator {

	private final IRTEngine irtEngine;
	private final BloomMasteryMapper bloomMasteryMapper;
	private final AdaptiveScheduler adaptiveScheduler;

	public static final double DEFAULT_DAMPING_FACTOR = BloomMasteryMapper.DEFAULT_DAMPING_FACTOR;
	public static final double[] LEVEL_MIN = BloomMasteryMapper.LEVEL_MIN;
	public static final double[] LEVEL_MAX = BloomMasteryMapper.LEVEL_MAX;
	public static final int ELO_PER_LEVEL = BloomMasteryMapper.ELO_PER_LEVEL;
	public static final String[] BLOOM_LEVELS = BloomMasteryMapper.BLOOM_LEVELS;

	// Alias for inner static class to maintain backward compatibility
	public static class UserAnswerGenerator extends com.example.demo.modules.quiz.evaluation.application.service.irt.UserAnswerGenerator {
	}

	public static String mapToBloom(double value) {
		return BloomMasteryMapper.mapToBloom(value);
	}

	// Support 3PL probabilities
	public double p3PL(double theta, double b, double a, double c) {
		return irtEngine.p3PL(theta, b, a, c);
	}

	public double p(double theta, double b) {
		return irtEngine.p(theta, b);
	}

	public double likelihood3PL(int x, double theta, double b, double a, double c) {
		return irtEngine.likelihood3PL(x, theta, b, a, c);
	}

	public double likelihood(int x, double theta, double b) {
		return irtEngine.likelihood(x, theta, b);
	}

	public double logLikelihood3PL(int x, double theta, double b, double a, double c) {
		return irtEngine.logLikelihood3PL(x, theta, b, a, c);
	}

	public double logLikelihood(int x, double theta, double b) {
		return irtEngine.logLikelihood(x, theta, b);
	}

	public double totalLogLikelihood3PL(List<UserAnswer> answers, double theta, double defaultA, double defaultC) {
		return irtEngine.totalLogLikelihood3PL(answers, theta, defaultA, defaultC);
	}

	public double totalLogLikelihood(List<UserAnswer> answers, double theta) {
		return irtEngine.totalLogLikelihood(answers, theta);
	}

	public double gradientMAP3PL(List<UserAnswer> answers, double theta, double sigma, double defaultA, double defaultC) {
		return irtEngine.gradientMAP3PL(answers, theta, sigma, defaultA, defaultC);
	}

	public double gradientMAP(List<UserAnswer> answers, double theta, double sigma) {
		return irtEngine.gradientMAP(answers, theta, sigma);
	}

	public double hessianMAP3PL(List<UserAnswer> answers, double theta, double sigma, double defaultA, double defaultC) {
		return irtEngine.hessianMAP3PL(answers, theta, sigma, defaultA, defaultC);
	}

	public double hessianMAP(List<UserAnswer> answers, double theta, double sigma) {
		return irtEngine.hessianMAP(answers, theta, sigma);
	}

	public double estimateThetaMAP3PL(List<UserAnswer> answers, double thetaInit, double sigma, double dampingFactor, double defaultA, double defaultC) {
		return irtEngine.estimateThetaMAP3PL(answers, thetaInit, sigma, dampingFactor, defaultA, defaultC);
	}

	public double estimateThetaMAP(List<UserAnswer> answers, double thetaInit, double sigma, double dampingFactor) {
		return irtEngine.estimateThetaMAP(answers, thetaInit, sigma, dampingFactor);
	}

	public double fisher3PL(double theta, double b, double a, double c) {
		return irtEngine.fisher3PL(theta, b, a, c);
	}

	public double fisher(double theta, double b) {
		return irtEngine.fisher(theta, b);
	}

	public double suggestDifficultyB(double thetaCurrent, double pTarget) {
		return adaptiveScheduler.suggestDifficultyB(thetaCurrent, pTarget);
	}

	public int[] allocateQuestionsByBloom(double theta, int totalQuestions, double dampingFactor) {
		return adaptiveScheduler.allocateQuestionsByBloom(theta, totalQuestions, dampingFactor);
	}

	public double recalibrateItemDifficulty(double currentB, double userTheta, boolean correct, double learningRate) {
		return adaptiveScheduler.recalibrateItemDifficulty(currentB, userTheta, correct, learningRate);
	}

	public int calculateMasteryLevel(double theta) {
		return bloomMasteryMapper.calculateMasteryLevel(theta);
	}

	public String getMasteryLabel(int level) {
		return bloomMasteryMapper.getMasteryLabel(level);
	}

	public int thetaToElo(double theta) {
		return bloomMasteryMapper.thetaToElo(theta);
	}

	public int thetaToEloInLevel(double theta) {
		return bloomMasteryMapper.thetaToEloInLevel(theta);
	}

	public int eloToNextLevel(double theta) {
		return bloomMasteryMapper.eloToNextLevel(theta);
	}

	/**
	 * Reviews a recent set of answers to update theta and generate the next optimal difficulty range.
	 */
	public double[] reviewAnswer(List<UserAnswer> answers, double thetaCurrent, List<UserAnswer> history) {
		double sigma = 1.0;
		double dampingFactor = DEFAULT_DAMPING_FACTOR;
		double b_min = 0;
		double b_max = 0;
		final double B_MIN_LIMIT = -3.0;
		final double B_MAX_LIMIT = 3.0;

		for (UserAnswer userAnswer : answers) {
			history.add(userAnswer);
		}

		double newTheta = estimateThetaMAP(history, thetaCurrent, sigma, dampingFactor);
		double pTarget = 0.8;
		double suggestedB = suggestDifficultyB(newTheta, pTarget);

		suggestedB = Math.max(suggestedB, B_MIN_LIMIT);
		suggestedB = Math.min(suggestedB, B_MAX_LIMIT);

		b_min = Math.max(suggestedB - 0.3, B_MIN_LIMIT);
		b_max = Math.min(suggestedB + 0.3, B_MAX_LIMIT);

		return new double[] { newTheta, b_min, b_max };
	}
}
