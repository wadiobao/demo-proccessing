package com.example.demo.modules.quiz.evaluation.application.service.irt;

import org.springframework.stereotype.Component;

/**
 * Handles the mapping between latent ability (Theta) and pedagogical frameworks
 * such as Bloom's Taxonomy levels and ELO ratings.
 */
@Component
public class BloomMasteryMapper {

	public static final double DEFAULT_DAMPING_FACTOR = 0.3;

	public static final double[] LEVEL_MIN = { -3.00, -1.10, -0.41, 0.20, 0.85, 1.73 };
	public static final double[] LEVEL_MAX = { -1.10, -0.41, 0.20, 0.85, 1.73, 3.00 };
	public static final int ELO_PER_LEVEL = 200;

	public static final String[] BLOOM_LEVELS = {
			"Remembering",
			"Understanding",
			"Applying",
			"Analyzing",
			"Evaluating",
			"Creating"
	};

	public static String mapToBloom(double value) {
		for (int i = 0; i < LEVEL_MAX.length - 1; i++) {
			if (value <= LEVEL_MAX[i]) {
				return BLOOM_LEVELS[i];
			}
		}
		return BLOOM_LEVELS[5];
	}

	public int calculateMasteryLevel(double theta) {
		for (int i = 0; i < LEVEL_MAX.length - 1; i++) {
			if (theta <= LEVEL_MAX[i]) {
				return i + 1;
			}
		}
		return 6;
	}

	public String getMasteryLabel(int level) {
		if (level < 1 || level > 6) {
			return "Unknown";
		}
		return BLOOM_LEVELS[level - 1];
	}

	public int thetaToElo(double theta) {
		int levelIdx = calculateMasteryLevel(theta) - 1;
		double span = LEVEL_MAX[levelIdx] - LEVEL_MIN[levelIdx];
		double eloInLevel = (theta - LEVEL_MIN[levelIdx]) / span * ELO_PER_LEVEL;
		int elo = (int) Math.round(levelIdx * ELO_PER_LEVEL + eloInLevel);
		return Math.max(0, Math.min(6 * ELO_PER_LEVEL, elo));
	}

	public int thetaToEloInLevel(double theta) {
		int levelIdx = calculateMasteryLevel(theta) - 1;
		double span = LEVEL_MAX[levelIdx] - LEVEL_MIN[levelIdx];
		double eloInLevel = (theta - LEVEL_MIN[levelIdx]) / span * ELO_PER_LEVEL;
		return (int) Math.round(Math.max(0, Math.min(ELO_PER_LEVEL, eloInLevel)));
	}

	public int eloToNextLevel(double theta) {
		if (calculateMasteryLevel(theta) >= 6) {
			return 0;
		}
		int eloInLevel = thetaToEloInLevel(theta);
		return ELO_PER_LEVEL - eloInLevel;
	}
}
