package com.example.demo.modules.quiz.evaluation.application.service.irt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

/**
 * Utility generator to create mock test sets of user interactions for simulations and tests.
 */
public class UserAnswerGenerator {

	private static final Random random = new Random();
	public static final String[] BLOOM_LEVELS = {
			"Remembering",
			"Understanding",
			"Applying",
			"Analyzing",
			"Evaluating",
			"Creating"
		};

	public enum AnswerPattern {
		ALL_TRUE,
		ALL_FALSE,
		HALF_TRUE,
		RANDOM
	}

	public static List<UserAnswer> generateOneSet(
			int numQuestions,
			float minDiff,
			float maxDiff,
			AnswerPattern pattern) {
		List<UserAnswer> answers = new ArrayList<>();

		for (int i = 1; i <= numQuestions; i++) {
			float diff = minDiff + random.nextFloat() * (maxDiff - minDiff);
			boolean correct = pickCorrect(pattern, i);
			String bloom = BLOOM_LEVELS[random.nextInt(BLOOM_LEVELS.length)];

			answers.add(UserAnswer.builder()
					.id("Q" + i)
					.correct(correct)
					.difficulty(diff)
					.bloomLevel(bloom)
					.build());
		}

		return answers;
	}

	private static boolean pickCorrect(AnswerPattern pattern, int index) {
		switch (pattern) {
			case ALL_TRUE:
				return true;
			case ALL_FALSE:
				return false;
			case HALF_TRUE:
				return index % 2 == 0;
			default:
				return random.nextBoolean();
		}
	}

	public static List<List<UserAnswer>> generateMultipleSets(
			int numSets,
			int numQuestions,
			float minDiff,
			float maxDiff,
			AnswerPattern pattern) {
		List<List<UserAnswer>> list = new ArrayList<>();
		for (int i = 0; i < numSets; i++) {
			list.add(generateOneSet(numQuestions, minDiff, maxDiff, pattern));
		}
		return list;
	}
}
