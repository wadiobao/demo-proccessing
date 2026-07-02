package com.example.demo.modules.quiz.evaluation.application.service.irt;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages item selection strategies, difficulty adjustments, and adaptive session configuration.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdaptiveScheduler {

	private final IRTEngine irtEngine;

	public double suggestDifficultyB(double thetaCurrent, double pTarget) {
		double logitTerm = Math.log((1.0 - pTarget) / pTarget);
		return thetaCurrent + logitTerm;
	}

	public int[] allocateQuestionsByBloom(double theta, int totalQuestions, double dampingFactor) {
		double[] bCenters = { -2.050, -0.755, -0.105, 0.525, 1.290, 2.365 };
		double[] weights = new double[6];
		for (int i = 0; i < 6; i++) {
			weights[i] = irtEngine.fisher(theta, bCenters[i]);
		}

		double uniform = 1.0 / 6.0;
		double weightSum = 0;
		for (int i = 0; i < 6; i++) {
			weights[i] = (1.0 - dampingFactor) * weights[i] + dampingFactor * uniform;
			weightSum += weights[i];
		}

		double[] proportions = new double[6];
		for (int i = 0; i < 6; i++) {
			proportions[i] = weights[i] / weightSum;
		}

		int[] counts = new int[6];
		double[] remainders = new double[6];
		int allocated = 0;

		for (int i = 0; i < 6; i++) {
			double exact = proportions[i] * totalQuestions;
			counts[i] = (int) exact;
			remainders[i] = exact - counts[i];
			allocated += counts[i];
		}

		int remaining = totalQuestions - allocated;
		for (int r = 0; r < remaining; r++) {
			int maxIdx = 0;
			for (int i = 1; i < 6; i++) {
				if (remainders[i] > remainders[maxIdx]) {
					maxIdx = i;
				}
			}
			counts[maxIdx]++;
			remainders[maxIdx] = -1.0;
		}

		log.debug("BloomAllocation θ={} N={}: Rem={} Und={} App={} Ana={} Eva={} Cre={}",
				theta, totalQuestions,
				counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);

		return counts;
	}

	public double recalibrateItemDifficulty(double currentB, double userTheta, boolean correct, double learningRate) {
		double pv = irtEngine.p(userTheta, currentB);
		double score = correct ? 1.0 : 0.0;
		double newB = currentB + learningRate * (pv - score);
		return Math.max(Math.min(newB, 3.0), -3.0);
	}
}
