package com.example.demo.modules.quiz.evaluation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.extern.slf4j.Slf4j;

/**
 * Mathematical engine for Item Response Theory (IRT) and adaptive testing.
 * 
 * <p>
 * Triển khai các thuật toán ước lượng năng lực người dùng (Theta)
 * bằng phương pháp MAP (Maximum A Posteriori) và hiệu chỉnh độ khó
 * câu hỏi (b) dựa trên phản hồi thực tế.
 *
 * @since 1.0
 */
@Component
@Slf4j
public class IRTCalculator {
	public double p(double theta, double b) {
		return 1.0 / (1.0 + Math.exp(-(theta - b)));
	}

	public double likelihood(int x, double theta, double b) {
		double p = p(theta, b);
		return x == 1 ? p : (1 - p);
	}

	public double logLikelihood(int x, double theta, double b) {
		double p = p(theta, b);
		return x * Math.log(p) + (1 - x) * Math.log(1 - p);
	}

	public double totalLogLikelihood(List<UserAnswer> answers, double theta) {
		double sum = 0;
		for (UserAnswer a : answers) {
			double p = p(theta, a.getDifficulty());
			sum += a.isTrue() ? Math.log(p) : Math.log(1 - p);
		}
		return sum;
	}

	/**
	 * Đạo hàm bậc nhất của Log-Posterior (Gradient MAP).
	 * G' = Likelihood' - (theta / sigma^2)
	 */
	public double gradientMAP(List<UserAnswer> answers, double theta, double sigma) {
		// Likelihood component: derivative of the probability of observed responses
		// (same as MLE gradient)
		// / Thành phần Likelihood: đạo hàm của xác suất các phản hồi đã quan sát (giống
		// gradient MLE)
		double g = 0;
		for (UserAnswer a : answers) {
			g += (a.isTrue() ? 1.0 : 0.0) - p(theta, a.getDifficulty());
		}

		// Prior component: gradient of Gaussian prior N(0, sigma^2) to prevent extreme
		// theta estimates
		// / Thành phần Tiên nghiệm: gradient của phân phối Gaussian N(0, sigma^2) để
		// tránh các ước tính theta cực đoan
		g -= theta / (sigma * sigma);

		return g;
	}

	/**
	 * Đạo hàm bậc hai của Log-Posterior (Hessian MAP).
	 * H'' = Likelihood'' - (1 / sigma^2)
	 */
	public double hessianMAP(List<UserAnswer> answers, double theta, double sigma) {
		// Hessian of Likelihood: curvature of the log-likelihood function
		// / Hessian của Likelihood: độ cong của hàm log-likelihood
		double h = 0;
		for (UserAnswer a : answers) {
			double pv = p(theta, a.getDifficulty());
			h -= pv * (1 - pv);
		}

		// Hessian of Prior: constant penalty for second derivative
		// / Hessian của Tiên nghiệm: mức phạt hằng số cho đạo hàm bậc hai
		h -= 1.0 / (sigma * sigma);

		return h;
	}

	/**
	 * Cập nhật Theta theo phương pháp Newton-Raphson cho MAP.
	 */
	/**
	 * Estimates user ability (Theta) using Maximum A Posteriori (MAP) with
	 * Newton-Raphson.
	 * 
	 * @param answers       list of recent user interactions / danh sách câu trả lời
	 *                      của người dùng
	 * @param thetaInit     starting value for optimization / giá trị theta khởi tạo
	 * @param sigma         prior standard deviation / độ lệch chuẩn của phân phối
	 *                      tiên nghiệm
	 * @param dampingFactor learning deceleration to prevent divergence / hệ số giảm
	 *                      tốc
	 * @return optimized ability estimate (Theta) / năng lực ước lượng tối ưu
	 */
	public double estimateThetaMAP(
			List<UserAnswer> answers,
			double thetaInit,
			double sigma,
			double dampingFactor) {
		double theta = thetaInit;
		double epsilon = 1e-6; // Ngưỡng hội tụ
		int maxIter = 100;

		for (int iter = 0; iter < maxIter; iter++) {
			double g = gradientMAP(answers, theta, sigma);
			double h = hessianMAP(answers, theta, sigma);

			// Newton step: -g / h (h is negative, so g/h moves towards the peak)
			// / Bước nhảy Newton: -g / h (h âm, nên g/h di chuyển về phía đỉnh)
			double step = g / h;

			// Dampen updates to enforce stability and avoid oscillating around the maximum
			// / Giảm tốc cập nhật để đảm bảo tính ổn định và tránh dao động quanh giá trị
			// cực đại
			double dampened_step = dampingFactor * step;

			theta -= dampened_step;

			// Floor/Ceiling constraints to keep theta within Rasch 1PL psychometric bounds [-3.0, +3.0]
			// / Giới hạn biên theo thang đo IRT Rasch 1PL để giữ theta trong khoảng đo lường chuẩn
			theta = Math.max(Math.min(theta, 3.0), -3.0);

			// stop when the update magnitude falls below precision threshold
			// / dừng khi độ lớn cập nhật rơi xuống dưới ngưỡng độ chính xác
			if (Math.abs(dampened_step) < epsilon) {
				break;
			}
		}
		return theta;
	}

	public double fisher(double theta, double b) {
		double p = p(theta, b);
		return p * (1 - p);
	}

	public double suggestDifficultyB(double thetaCurrent, double pTarget) {
		// inversion of the Logistic function to find difficulty that yields target
		// probability
		// / nghịch đảo hàm Logistic để tìm độ khó mang lại xác suất mục tiêu
		double logitTerm = Math.log((1.0 - pTarget) / pTarget);
		return thetaCurrent + logitTerm;
	}

	/**
	 * Allocates a fixed number of questions across six Bloom taxonomy levels
	 * using Fisher Information Weighting.
	 *
	 * <p>
	 * The Fisher Information I(θ, b) = P(θ,b) × (1 − P(θ,b)) peaks when b = θ,
	 * meaning the method naturally concentrates questions at the difficulty level
	 * that is most informative for the user's current ability.
	 *
	 * <p>
	 * A uniform floor (alpha) is mixed in to guarantee that no Bloom level is
	 * completely skipped — every level receives at least a small exposure even when
	 * far from the user's θ.
	 *
	 * <p>
	 * Rounding uses the Largest Remainder Method to ensure the sum equals exactly
	 * {@code totalQuestions}.
	 *
	 * @param theta          current user ability estimate (-3.0 to +3.0)
	 * @param totalQuestions session size; recommended values: 15, 30, 50
	 * @param alpha          uniform floor weight (0.0 = pure Fisher, 0.10 recommended)
	 * @return int[6] where index maps to: [Remembering, Understanding, Applying,
	 *         Analyzing, Evaluating, Creating]
	 */
	public int[] allocateQuestionsByBloom(double theta, int totalQuestions, double alpha) {
		// Midpoint of each Bloom level's difficulty range on the IRT [-3.0, +3.0] scale
		double[] bCenters = { -2.050, -0.755, -0.105, 0.525, 1.290, 2.365 };

		// Step 1: Compute raw Fisher Information at each Bloom center
		double[] weights = new double[6];
		for (int i = 0; i < 6; i++) {
			weights[i] = fisher(theta, bCenters[i]);
		}

		// Step 2: Mix Fisher weights with a uniform floor to guarantee coverage
		// alpha=0.10 → 90% ability-adaptive + 10% flat exposure across all levels
		double uniform = 1.0 / 6.0;
		double weightSum = 0;
		for (int i = 0; i < 6; i++) {
			weights[i] = (1.0 - alpha) * weights[i] + alpha * uniform;
			weightSum += weights[i];
		}

		// Step 3: Normalize to proportions
		double[] proportions = new double[6];
		for (int i = 0; i < 6; i++) {
			proportions[i] = weights[i] / weightSum;
		}

		// Step 4: Convert to integer counts using Largest Remainder Method
		// to guarantee the total sums exactly to totalQuestions
		int[] counts = new int[6];
		double[] remainders = new double[6];
		int allocated = 0;

		for (int i = 0; i < 6; i++) {
			double exact = proportions[i] * totalQuestions;
			counts[i] = (int) exact;
			remainders[i] = exact - counts[i];
			allocated += counts[i];
		}

		// Distribute remaining slots to levels with largest fractional remainders
		int remaining = totalQuestions - allocated;
		for (int r = 0; r < remaining; r++) {
			int maxIdx = 0;
			for (int i = 1; i < 6; i++) {
				if (remainders[i] > remainders[maxIdx]) {
					maxIdx = i;
				}
			}
			counts[maxIdx]++;
			// zero out so we don't pick the same index twice
			remainders[maxIdx] = -1.0;
		}

		log.debug("BloomAllocation θ={} N={}: Rem={} Und={} App={} Ana={} Eva={} Cre={}",
				theta, totalQuestions,
				counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);

		return counts;
	}


	/**
	 * Recalibrates a question's difficulty (b) based on a single user interaction.
	 * Moves b in the direction that maximizes Likelihood.
	 * 
	 * @param currentB     Current difficulty parameter
	 * @param userTheta    User's theta (ability)
	 * @param correct      Whether user got it right
	 * @param learningRate Adjustment factor (e.g., 0.1)
	 * @return Updated difficulty parameter
	 */
	/**
	 * Recalibrates a question's difficulty (b) based on an empirical user
	 * interaction.
	 * 
	 * @param currentB     Current difficulty parameter / độ khó hiện tại của câu
	 *                     hỏi
	 * @param userTheta    User's latent ability / năng lực thực tế của người dùng
	 * @param correct      binary outcome of the attempt / kết quả (đúng/sai)
	 * @param learningRate weight of this specific interaction / hệ số học tập
	 * @return adjusted difficulty (b) / độ khó mới đã được hiệu chỉnh
	 */
	public double recalibrateItemDifficulty(double currentB, double userTheta, boolean correct, double learningRate) {
		double pv = p(userTheta, currentB);
		double score = correct ? 1.0 : 0.0;
		// move b in the direction of the prediction error (stochastic gradient descent)
		// / di chuyển b theo hướng sai số dự đoán (gradient descent ngẫu nhiên)
		double newB = currentB + learningRate * (pv - score);
		// sanitize output within valid IRT model range
		// / làm sạch kết quả trong phạm vi mô hình IRT hợp lệ
		return Math.max(Math.min(newB, 3.0), -3.0);
	}

	public static class UserAnswerGenerator {

		private static final Random random = new Random();
		private static final String[] BLOOM_LEVELS = {
				"Remembering", "Understanding", "Applying",
				"Analyzing", "Evaluating", "Creating"
		};

		public enum AnswerPattern {
			ALL_TRUE,
			ALL_FALSE,
			HALF_TRUE,
			RANDOM
		}

		// Tạo 1 danh sách answer
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
						.isTrue(correct)
						.difficulty(diff)
						.bloomLevel(bloom)
						.build());
			}

			return answers;
		}

		// Quy tắc đúng/sai
		private static boolean pickCorrect(AnswerPattern pattern, int index) {
			switch (pattern) {
				case ALL_TRUE:
					return true;
				case ALL_FALSE:
					return false;
				case HALF_TRUE:
					return index % 2 == 0; // nửa đúng nửa sai
				default:
					return random.nextBoolean();
			}
		}

		// Tạo nhiều bộ dữ liệu
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

	public double[] reviewAnswer(List<UserAnswer> answers, double thetaCurrent, List<UserAnswer> history) {
		double sigma = 1.0;
		double alpha = 0.5;
		double b_min = 0;
		double b_max = 0;
		final double B_MIN_LIMIT = -3.0;
		final double B_MAX_LIMIT = 3.0;

		for (int i = 0; i < answers.size(); i++) {

			// 1. Gợi ý độ khó theo P(target)
			double pTarget = 0.8;
			double suggestedB = suggestDifficultyB(thetaCurrent, pTarget);

			suggestedB = Math.max(suggestedB, B_MIN_LIMIT);
			suggestedB = Math.min(suggestedB, B_MAX_LIMIT);

			// 2. Tạo khoảng B đề xuất
			b_min = Math.max(suggestedB - 0.3, B_MIN_LIMIT);
			b_max = Math.min(suggestedB + 0.3, B_MAX_LIMIT);

			double actualB = (b_min + b_max) / 2.0;

			// 3. Mô phỏng trả lời dựa trên actual B và theta
			// double probCorrect = p(thetaCurrent, actualB);

			// 4. Lấy dữ liệu người dùng hiện tại
			UserAnswer userAnswer = answers.get(i);

			// Ghi vào lịch sử
			history.add(userAnswer);

			// 5. Tính theta mới (MAP)
			double newTheta = estimateThetaMAP(history, thetaCurrent, sigma, alpha);

			// 6. override actualB bằng độ khó thật mà userAnswer mang theo
			// actualB = userAnswer.getDifficulty();

			thetaCurrent = newTheta;
		}

		return new double[] { thetaCurrent, b_min, b_max };
	}

}
