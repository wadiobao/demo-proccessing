package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.demo.mongo.dto.question.UserAnswer;

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

			// Floor/Ceiling constraints to keep theta within reasonable psychometric bounds
			// / Giới hạn biên để giữ theta trong khoảng đo lường tâm lý học hợp lý
			theta = Math.max(Math.min(theta, 4.0), -4.0);

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
		final double B_MIN_LIMIT = -2.95;
		final double B_MAX_LIMIT = 2.94;

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

	public static void main(String[] args) {
		IRTCalculator calc = new IRTCalculator();
		List<UserAnswer> history = new ArrayList<>();
		List<UserAnswer> userAnswers = new ArrayList<UserAnswer>();
		boolean temp = true;
		userAnswers.add(UserAnswer.builder().id("1").isTrue(temp).difficulty(-1.85).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("2").isTrue(temp).difficulty(-0.75).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("3").isTrue(temp).difficulty(-1.55).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("4").isTrue(temp).difficulty(-0.92).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("5").isTrue(temp).difficulty(-2.1).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("6").isTrue(temp).difficulty(-0.68).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("7").isTrue(temp).difficulty(-2.5).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("8").isTrue(temp).difficulty(-1.05).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("9").isTrue(temp).difficulty(-1.33).bloomLevel("Remembering").build());
		userAnswers.add(UserAnswer.builder().id("10").isTrue(temp).difficulty(-1.98).bloomLevel("Remembering").build());
		userAnswers.add(UserAnswer.builder().id("11").isTrue(temp).difficulty(-1.45).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("12").isTrue(false).difficulty(-0.88).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("13").isTrue(temp).difficulty(-2.3).bloomLevel("Remembering").build());
		userAnswers.add(UserAnswer.builder().id("14").isTrue(temp).difficulty(-1.2).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("15").isTrue(false).difficulty(-2.05).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("16").isTrue(false).difficulty(-0.5).bloomLevel("Understanding").build());
		userAnswers.add(UserAnswer.builder().id("17").isTrue(temp).difficulty(-2.4).bloomLevel("Remembering").build());
		userAnswers.add(UserAnswer.builder().id("18").isTrue(temp).difficulty(-1.25).bloomLevel("Remembering").build());
		userAnswers.add(UserAnswer.builder().id("19").isTrue(false).difficulty(-1.7).bloomLevel("Remembering").build());
		userAnswers
				.add(UserAnswer.builder().id("20").isTrue(temp).difficulty(-0.6).bloomLevel("Understanding").build());
		double thetaCurrent = 2.0;
		double sigma = 1.0;
		double alpha = 0.5;
		int numQuestionsToSimulate = 20; // Giả lập 50 câu hỏi
		final double B_MIN_LIMIT = -2.95;
		final double B_MAX_LIMIT = 2.94;

		// --- Vòng lặp chính ---
		for (int i = 0; i < numQuestionsToSimulate; i++) {
			// 1. Đề xuất độ khó B cho câu hỏi tiếp theo
			double pTarget = 0.8;
			double suggestedB = calc.suggestDifficultyB(thetaCurrent, pTarget);

			suggestedB = Math.max(suggestedB, B_MIN_LIMIT);
			suggestedB = Math.min(suggestedB, B_MAX_LIMIT);

			// 2. Giới hạn phạm vi độ khó
			double b_min = Math.max((suggestedB - 0.3), B_MIN_LIMIT);
			double b_max = Math.min((suggestedB + 0.3), B_MAX_LIMIT);

			// 3. TẠO CÂU HỎI MỚI VÀ TRẢ LỜI MÔ PHỎNG (Chỉ 1 câu hỏi/lần lặp)
			// Lưu ý: Thay vì dùng RANDOM, nên dùng mô hình P(theta) để xác định đúng/sai
			// dựa trên thetaCurrent và b đề xuất.

			// Giả lập 1 câu hỏi (với độ khó b trung bình của phạm vi đề xuất)
			double actualB = (b_min + b_max) / 2.0;

			// ** Mô phỏng Trả lời (Tạo dữ liệu)**
			// Dùng P(thetaCurrent, actualB) để xác định xác suất, sau đó dùng Random để
			// chọn True/False
			double probabilityOfCorrect = calc.p(thetaCurrent, actualB);
			// boolean isCorrect = new Random().nextDouble() < probabilityOfCorrect; // This
			// line was commented out in the provided edit, keeping it that way.

			// 4. Lưu câu trả lời vào lịch sử
			UserAnswer newAnswer = userAnswers.get(i);

			history.add(newAnswer);

			// 5. Cập nhật Theta (MAP) dựa trên TOÀN BỘ lịch sử
			double newTheta = calc.estimateThetaMAP(history, thetaCurrent, sigma, alpha);
			actualB = newAnswer.getDifficulty();

			log.debug("Fisher information: {}", calc.fisher(thetaCurrent, actualB));
			log.debug("Current Theta: {}", thetaCurrent);
			log.debug("Current Item Difficulty (b): {}", actualB);
			log.info("Iteration {}: suggestedB={}, P_correct={}, Result={}, New Theta={}",
					i + 1, suggestedB, probabilityOfCorrect, newAnswer.isTrue() ? "CORRECT" : "WRONG", newTheta);

			// 6. Cập nhật theta cho lần lặp tiếp theo
			thetaCurrent = newTheta;

			// Dừng khi theta quá cao (ví dụ)
			if (thetaCurrent > 2) {
				log.info("Theta threshold (> 2.0) reached. Terminating simulation.");
				break;
			}
		}
	}

}
