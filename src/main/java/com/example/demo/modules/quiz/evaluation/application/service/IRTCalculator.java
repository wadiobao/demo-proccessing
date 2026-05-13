package com.example.demo.modules.quiz.evaluation.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;

import lombok.extern.slf4j.Slf4j;

/**
 * Mathematical engine for estimating user ability and adjusting question
 * difficulty in adaptive assessments.
 *
 * <p>
 * [EN] This class implements the Maximum A Posteriori (MAP) estimation using
 * the 1-Parameter Logistic (1PL)
 * Item Response Theory (IRT) model, commonly known as the Rasch Model. It
 * continuously evaluates a user's latent
 * ability (Theta) based on their interaction history, where the probability of
 * a correct answer depends solely
 * on item difficulty (b) and user ability (theta). It is responsible for
 * determining optimal question difficulty,
 * recalibrating item parameters, and mapping ability scores to mastery levels.
 *
 * <p>
 * [VI] Lớp thực thi thuật toán Cảm ứng Câu hỏi (IRT) sử dụng Mô hình Logistic 1
 * tham số (1PL) - hay còn gọi
 * là Mô hình Rasch. Xác suất trả lời đúng được tính toán hoàn toàn dựa trên độ
 * khó câu hỏi (b) và năng lực
 * người dùng (theta). Cùng với phương pháp Maximum A Posteriori (MAP), lớp này
 * liên tục đánh giá năng lực thực sự
 * của người dùng, gợi ý độ khó tối ưu cho câu hỏi tiếp theo, hiệu chỉnh độ khó
 * từ thực tế và ánh xạ điểm năng lực.
 *
 * <p>
 * TODO: Upgrade to Pseudo-3PL / 2PL Model when large empirical data is
 * available.
 * Currently uses 1PL to ensure mathematical convergence and stability with
 * sparse data.
 * Once massive dataset is collected (>10,000 interactions per item):
 * 1. Extract data to perform offline Batch Processing (e.g., using Expectation
 * Maximization in Python).
 * 2. Pre-calibrate fixed parameters for 'a' (Discrimination) and 'c' (Guessing,
 * e.g., 0.25 for 4-option questions).
 * 3. Update the probability function `p()`, `gradientMAP`, and `hessianMAP` to
 * reflect the 3PL mathematics.
 * 4. Keep online stochastic gradient descent ONLY for item difficulty 'b'.
 *
 * @since 1.0
 */
@Component
@Slf4j
public class IRTCalculator {

	/** Default damping factor used in MAP optimization to prevent theta from diverging too fast.
	 * / Hệ số giảm xóc mặc định trong tối ưu hóa MAP, giữ theta không nhảy quá mạnh trong mỗi bước. */
	public static final double DEFAULT_DAMPING_FACTOR = 0.3;

	/**
	 * [EN] Lower boundary of each Bloom mastery level on the IRT theta scale.
	 * [VI] Cận dưới của từng cấp độ thành thạo Bloom trên thang đo theta IRT.
	 * Source: base-bloom-irt.txt
	 */
	public static final double[] LEVEL_MIN = { -3.00, -1.10, -0.41, 0.20, 0.85, 1.73 };

	/**
	 * [EN] Upper boundary (inclusive) of each Bloom mastery level on the IRT theta scale.
	 * [VI] Cận trên (bao gồm) của từng cấp độ thành thạo Bloom trên thang đo theta IRT.
	 */
	public static final double[] LEVEL_MAX = { -1.10, -0.41, 0.20, 0.85, 1.73, 3.00 };

	/** ELO points allocated per Bloom level / Số ELO phân bổ cho mỗi cấp độ Bloom. */
	public static final int ELO_PER_LEVEL = 200;

	/**
	 * Computes the probability of a correct answer using the 1-Parameter Logistic
	 * (Rasch) Model.
	 *
	 * <p>
	 * [EN] Calculates the success probability based on the difference between user
	 * ability and item difficulty.
	 * <p>
	 * [VI] Tính toán xác suất trả lời đúng dựa trên chênh lệch giữa năng lực người
	 * dùng và độ khó câu hỏi (Mô hình Rasch 1 tham số).
	 *
	 * @param theta user's latent ability / năng lực thực tế của người dùng
	 * @param b     item difficulty / độ khó của câu hỏi
	 * @return probability of a correct answer [0.0 - 1.0] / xác suất trả lời đúng
	 *         [0.0 - 1.0]
	 */
	public double p(double theta, double b) {
		return 1.0 / (1.0 + Math.exp(-(theta - b)));
	}

	/**
	 * Computes the likelihood of a specific response (correct or incorrect).
	 *
	 * <p>
	 * [EN] Returns the probability of the observed outcome given the user's ability
	 * and item difficulty.
	 * <p>
	 * [VI] Trả về xác suất xảy ra của kết quả đã quan sát (đúng hoặc sai) dựa trên
	 * năng lực và độ khó.
	 *
	 * @param x     observed binary outcome (1 = correct, 0 = incorrect) / kết quả
	 *              quan sát (1 = đúng, 0 = sai)
	 * @param theta user's latent ability / năng lực thực tế của người dùng
	 * @param b     item difficulty / độ khó của câu hỏi
	 * @return likelihood of the outcome / khả năng xảy ra của kết quả
	 */
	public double likelihood(int x, double theta, double b) {
		double p = p(theta, b);
		return x == 1 ? p : (1 - p);
	}

	/**
	 * Computes the natural logarithm of the likelihood function.
	 *
	 * <p>
	 * [EN] Used for numerical stability during mathematical optimization (MLE/MAP).
	 * <p>
	 * [VI] Sử dụng logarit tự nhiên của hàm khả năng để đảm bảo tính ổn định số học
	 * khi tối ưu hóa.
	 *
	 * @param x     observed binary outcome (1 = correct, 0 = incorrect) / kết quả
	 *              quan sát (1 = đúng, 0 = sai)
	 * @param theta user's latent ability / năng lực thực tế của người dùng
	 * @param b     item difficulty / độ khó của câu hỏi
	 * @return log-likelihood value / giá trị log-likelihood
	 */
	public double logLikelihood(int x, double theta, double b) {
		double p = p(theta, b);
		return x * Math.log(p) + (1 - x) * Math.log(1 - p);
	}

	/**
	 * Computes the aggregate log-likelihood of a sequence of responses.
	 *
	 * <p>
	 * [EN] Sums the log-likelihoods of all past interactions to evaluate how well
	 * the current theta explains the history.
	 * <p>
	 * [VI] Tính tổng giá trị log-likelihood của toàn bộ lịch sử trả lời để đánh giá
	 * độ khớp của điểm Theta hiện tại.
	 *
	 * @param answers list of user responses / danh sách câu trả lời của người dùng
	 * @param theta   current ability estimate / ước lượng năng lực hiện tại
	 * @return total log-likelihood / tổng giá trị log-likelihood
	 */
	public double totalLogLikelihood(List<UserAnswer> answers, double theta) {
		double sum = 0;
		for (UserAnswer a : answers) {
			double p = p(theta, a.getDifficulty());
			sum += a.isCorrect() ? Math.log(p) : Math.log(1 - p);
		}
		return sum;
	}

	/**
	 * Computes the first derivative (Gradient) of the Log-Posterior distribution.
	 *
	 * <p>
	 * [EN] Calculates the slope of the likelihood curve to determine the direction
	 * of the optimization step.
	 * <p>
	 * [VI] Đạo hàm bậc nhất (Gradient) của hàm Log-Posterior. Xác định độ dốc để
	 * tìm hướng tối ưu hóa Theta.
	 *
	 * @param answers list of user responses / danh sách câu trả lời của người dùng
	 * @param theta   current ability estimate / ước lượng năng lực hiện tại
	 * @param sigma   prior standard deviation to penalize extreme values / độ lệch
	 *                chuẩn tiên nghiệm để phạt các giá trị cực đoan
	 * @return the gradient slope / độ dốc của hàm mục tiêu
	 */
	public double gradientMAP(List<UserAnswer> answers, double theta, double sigma) {
		// Likelihood component: derivative of the probability of observed responses
		// / Thành phần Likelihood: đạo hàm của xác suất các phản hồi đã quan sát
		double g = 0;
		for (UserAnswer a : answers) {
			g += (a.isCorrect() ? 1.0 : 0.0) - p(theta, a.getDifficulty());
		}

		// Prior component: gradient of Gaussian prior N(0, sigma^2)
		// / Thành phần Tiên nghiệm: gradient của phân phối Gaussian N(0, sigma^2)
		g -= theta / (sigma * sigma);

		return g;
	}

	/**
	 * Computes the second derivative (Hessian) of the Log-Posterior distribution.
	 *
	 * <p>
	 * [EN] Calculates the curvature of the likelihood function to determine the
	 * step size for Newton-Raphson.
	 * <p>
	 * [VI] Đạo hàm bậc hai (Hessian) của hàm Log-Posterior. Đo lường độ cong của
	 * hàm để xác định bước nhảy tối ưu (Newton-Raphson).
	 *
	 * @param answers list of user responses / danh sách câu trả lời của người dùng
	 * @param theta   current ability estimate / ước lượng năng lực hiện tại
	 * @param sigma   prior standard deviation to penalize extreme values / độ lệch
	 *                chuẩn tiên nghiệm để phạt các giá trị cực đoan
	 * @return the negative curvature value / giá trị độ cong (luôn âm)
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
	 * Estimates the user's latent ability (Theta) using Maximum A Posteriori (MAP)
	 * optimization.
	 *
	 * <p>
	 * [EN] Determines the most probable ability score that explains the user's
	 * entire response history,
	 * bounded by a Gaussian prior to prevent extreme ability fluctuations during
	 * early assessment stages.
	 * 
	 * <p>
	 * [VI] Ước lượng điểm năng lực (Theta) của người dùng bằng cách tìm giá trị tối
	 * ưu nhất khớp với toàn bộ
	 * lịch sử làm bài. Sử dụng phân phối tiên nghiệm (Prior) để ngăn chặn điểm số
	 * biến động quá mạnh ở những câu đầu tiên.
	 *
	 * @param answers       history of user responses / lịch sử các câu trả lời của
	 *                      người dùng
	 * @param thetaInit     initial estimate for mathematical optimization / giá trị
	 *                      bắt đầu để chạy tối ưu hóa
	 * @param sigma         prior variance to penalize extreme values / độ lệch
	 *                      chuẩn của hàm phạt tiên nghiệm
	 * @param dampingFactor coefficient to stabilize the mathematical convergence /
	 *                      hệ số giảm tốc để giữ ổn định thuật toán
	 * @return the recalibrated latent ability estimate / điểm năng lực mới nhất đã
	 *         được tính toán
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

			// Floor/Ceiling constraints to keep theta within Rasch 1PL psychometric bounds
			// [-3.0, +3.0]
			// / Giới hạn biên theo thang đo IRT Rasch 1PL để giữ theta trong khoảng đo
			// lường chuẩn
			theta = Math.max(Math.min(theta, 3.0), -3.0);

			// stop when the update magnitude falls below precision threshold
			// / dừng khi độ lớn cập nhật rơi xuống dưới ngưỡng độ chính xác
			if (Math.abs(dampened_step) < epsilon) {
				break;
			}
		}
		return theta;
	}

	/**
	 * Computes the Fisher Information for a given ability and difficulty.
	 *
	 * <p>
	 * [EN] Quantifies how much information an item provides about a user's ability.
	 * The information peaks when ability equals difficulty.
	 * <p>
	 * [VI] Tính toán lượng thông tin Fisher. Đại lượng này đạt đỉnh (cho nhiều
	 * thông tin nhất) khi độ khó câu hỏi bằng đúng với năng lực người dùng.
	 *
	 * @param theta user's latent ability / năng lực thực tế của người dùng
	 * @param b     item difficulty / độ khó của câu hỏi
	 * @return the amount of statistical information / lượng thông tin thống kê thu
	 *         được
	 */
	public double fisher(double theta, double b) {
		double p = p(theta, b);
		return p * (1 - p);
	}

	/**
	 * Computes the optimal target difficulty (b) to maintain user engagement.
	 *
	 * <p>
	 * [EN] Resolves the difficulty parameter required to achieve a specific
	 * probability of success,
	 * ensuring the assessment remains appropriately challenging based on current
	 * ability.
	 * 
	 * <p>
	 * [VI] Tính toán độ khó câu hỏi (b) hoàn hảo nhất để người dùng đạt được tỷ lệ
	 * trả lời đúng theo kỳ vọng,
	 * giúp duy trì động lực học tập mà không bị quá khó hay quá dễ.
	 *
	 * @param thetaCurrent current user ability / năng lực hiện tại của người dùng
	 * @param pTarget      desired probability of a correct answer / tỷ lệ trả lời
	 *                     đúng mục tiêu (ví dụ 0.8)
	 * @return the suggested difficulty parameter (b) / mức độ khó câu hỏi được đề
	 *         xuất
	 */
	public double suggestDifficultyB(double thetaCurrent, double pTarget) {
		// inversion of the Logistic function to find difficulty that yields target
		// probability
		// / nghịch đảo hàm Logistic để tìm độ khó mang lại xác suất mục tiêu
		double logitTerm = Math.log((1.0 - pTarget) / pTarget);
		return thetaCurrent + logitTerm;
	}

	/**
	 * Allocates a fixed number of questions across six Bloom taxonomy levels using
	 * Fisher Information Weighting.
	 *
	 * <p>
	 * [EN] Distributes questions based on where the system can gain the most
	 * mathematical information about the user,
	 * while ensuring no cognitive level is completely ignored through a uniform
	 * floor penalty.
	 * <p>
	 * [VI] Phân bổ số lượng câu hỏi cho 6 cấp độ tư duy Bloom dựa trên lượng thông
	 * tin Fisher.
	 * Hệ thống sẽ tập trung hỏi nhiều ở những cấp độ phù hợp với năng lực hiện tại
	 * để đo lường chính xác nhất,
	 * nhưng vẫn giữ một tỷ lệ nhỏ (dampingFactor ) chia đều cho tất cả các cấp độ
	 * để tránh bỏ sót.
	 *
	 * @param theta          current user ability estimate (-3.0 to +3.0) / năng lực
	 *                       hiện tại của người dùng
	 * @param totalQuestions total number of questions in the session / tổng số câu
	 *                       hỏi trong phiên thi
	 * @param dampingFactor  uniform floor weight (e.g. 0.10) / tỷ lệ phân bổ đồng
	 *                       đều tối thiểu (để chống bỏ sót)
	 * @return array of question counts for each Bloom level / mảng chứa số lượng
	 *         câu hỏi phân bổ cho từng cấp độ Bloom
	 */
	public int[] allocateQuestionsByBloom(double theta, int totalQuestions, double dampingFactor) {
		// [EN] Midpoint of each Bloom level's difficulty range on the IRT [-3.0, +3.0]
		// scale
		// [VI] Điểm giữa của dải độ khó cho từng mức độ nhận thức Bloom trên thang đo
		// IRT [-3.0, +3.0]
		double[] bCenters = { -2.050, -0.755, -0.105, 0.525, 1.290, 2.365 };

		// [EN] Step 1: Compute raw Fisher Information at each Bloom center
		// [VI] Bước 1: Tính toán lượng thông tin Fisher gốc tại mỗi điểm giữa của cấp
		// độ Bloom
		double[] weights = new double[6];
		for (int i = 0; i < 6; i++) {
			weights[i] = fisher(theta, bCenters[i]);
		}

		// [EN] Step 2: Mix Fisher weights with a uniform floor to guarantee coverage
		// [EN] dampingFactor =0.10 → 90% ability-adaptive + 10% flat exposure across
		// all levels
		// [VI] Bước 2: Trộn trọng số Fisher với một mức sàn phân bổ đều để đảm bảo mọi
		// cấp độ đều được hỏi
		// [VI] dampingFactor =0.10 → 90% thích ứng theo năng lực + 10% rải đều cho các
		// mức Bloom
		double uniform = 1.0 / 6.0;
		double weightSum = 0;
		for (int i = 0; i < 6; i++) {
			weights[i] = (1.0 - dampingFactor) * weights[i] + dampingFactor * uniform;
			weightSum += weights[i];
		}

		// [EN] Step 3: Normalize to proportions
		// [VI] Bước 3: Chuẩn hóa trọng số thành tỷ lệ phần trăm
		double[] proportions = new double[6];
		for (int i = 0; i < 6; i++) {
			proportions[i] = weights[i] / weightSum;
		}

		// [EN] Step 4: Convert to integer counts using Largest Remainder Method
		// [EN] to guarantee the total sums exactly to totalQuestions
		// [VI] Bước 4: Chuyển đổi thành số lượng câu hỏi (số nguyên) bằng phương pháp
		// Phần dư lớn nhất
		// [VI] để đảm bảo tổng số lượng chia ra bằng chính xác với tham số
		// totalQuestions
		int[] counts = new int[6];
		double[] remainders = new double[6];
		int allocated = 0;

		for (int i = 0; i < 6; i++) {
			double exact = proportions[i] * totalQuestions;
			counts[i] = (int) exact;
			remainders[i] = exact - counts[i];
			allocated += counts[i];
		}

		// [EN] Distribute remaining slots to levels with largest fractional remainders
		// [VI] Phân bổ các suất câu hỏi còn dư vào những cấp độ có phần dư lớn nhất
		int remaining = totalQuestions - allocated;
		for (int r = 0; r < remaining; r++) {
			int maxIdx = 0;
			for (int i = 1; i < 6; i++) {
				if (remainders[i] > remainders[maxIdx]) {
					maxIdx = i;
				}
			}
			counts[maxIdx]++;
			// [EN] zero out so we don't pick the same index twice
			// [VI] đặt phần dư về âm để không chọn lại chỉ số đó ở vòng lặp sau
			remainders[maxIdx] = -1.0;
		}

		log.debug("BloomAllocation θ={} N={}: Rem={} Und={} App={} Ana={} Eva={} Cre={}",
				theta, totalQuestions,
				counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);

		return counts;
	}

	/**
	 * Recalibrates a question's difficulty (b) based on an empirical user
	 * interaction.
	 *
	 * <p>
	 * [EN] Uses stochastic gradient descent to adjust the item's difficulty based
	 * on whether the user's actual
	 * performance matched the predicted outcome.
	 * <p>
	 * [VI] Tinh chỉnh lại độ khó của câu hỏi dựa trên thực tế. Nếu người dùng làm
	 * sai một câu được dự đoán là dễ,
	 * độ khó của câu đó sẽ được tăng lên và ngược lại.
	 * 
	 * @param currentB     Current difficulty parameter / độ khó hiện tại của câu
	 *                     hỏi
	 * @param userTheta    User's latent ability / năng lực ước lượng của người dùng
	 * @param correct      binary outcome of the attempt / kết quả (đúng/sai)
	 * @param learningRate weight of this specific interaction / hệ số học tập (bước
	 *                     nhảy)
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

	/**
	 * Maps an IRT theta score to a discrete mastery level on a 6-point Bloom scale.
	 *
	 * <p>[EN] Uses the actual non-uniform Bloom level boundaries from the item bank configuration.
	 * Each level boundary is right-closed (upper bound is inclusive).
	 * <p>[VI] Dùng biên giới không đều của từng cấp Bloom theo cấu hình ngân hàng câu hỏi.
	 * Biên trên của mỗi cấp là inclusive (kép kín bên phải).
	 *
	 * @param theta user's latent ability / năng lực thực tế của người dùng
	 * @return mastery level from 1 (Remembering) to 6 (Creating)
	 */
	public int calculateMasteryLevel(double theta) {
		// [EN] Walk through level boundaries; each level's upper bound is inclusive
		// [VI] Duyệt qua các biên cấp; cận trên của mỗi cấp là bao gồm (inclusive)
		for (int i = 0; i < LEVEL_MAX.length - 1; i++) {
			if (theta <= LEVEL_MAX[i]) {
				return i + 1;
			}
		}
		// [EN] Theta above all intermediate bounds falls into Level 6 (Creating)
		// [VI] Theta vượt qua tất cả biên trung gian →0 Level 6 (Creating)
		return 6;
	}

	public String getMasteryLabel(int level) {
		if (level < 1 || level > 6)
			return "Unknown";
		return UserAnswerGenerator.BLOOM_LEVELS[level - 1];
	}

	/**
	 * Converts a theta score to a total ELO score (0–1200) across all Bloom levels.
	 *
	 * <p>[EN] Each Bloom level contributes 200 ELO points. Progress within a level
	 * is normalized by that level’s own theta span, which is non-uniform.
	 * <p>[VI] Mỗi cấp Bloom đóng góp 200 điểm ELO. Tiến trình trong cấp được chuẩn hóa
	 * theo span theta riêng của cấp đó (không đều nhau).
	 *
	 * @param theta user’s latent ability / năng lực của người dùng
	 * @return total ELO score in range [0, 1200]
	 */
	public int thetaToElo(double theta) {
		int levelIdx = calculateMasteryLevel(theta) - 1; // 0-indexed
		double span = LEVEL_MAX[levelIdx] - LEVEL_MIN[levelIdx];
		double eloInLevel = (theta - LEVEL_MIN[levelIdx]) / span * ELO_PER_LEVEL;
		int elo = (int) Math.round(levelIdx * ELO_PER_LEVEL + eloInLevel);
		return Math.max(0, Math.min(6 * ELO_PER_LEVEL, elo));
	}

	/**
	 * Returns the ELO score within the user’s current Bloom mastery level (0–200).
	 *
	 * <p>[EN] Useful for displaying a progress bar within the current level.
	 * <p>[VI] Dùng để hiển thị thanh tiến trình trong cấp độ hiện tại.
	 *
	 * @param theta user’s latent ability / năng lực của người dùng
	 * @return ELO progress within the current level [0, 200]
	 */
	public int thetaToEloInLevel(double theta) {
		int levelIdx = calculateMasteryLevel(theta) - 1;
		double span = LEVEL_MAX[levelIdx] - LEVEL_MIN[levelIdx];
		double eloInLevel = (theta - LEVEL_MIN[levelIdx]) / span * ELO_PER_LEVEL;
		return (int) Math.round(Math.max(0, Math.min(ELO_PER_LEVEL, eloInLevel)));
	}

	/**
	 * Returns how many ELO points remain to reach the next mastery level.
	 *
	 * <p>[EN] Returns 0 if the user is already at the maximum level (Creating).
	 * <p>[VI] Trả về 0 nếu người dùng đã ở cấp tối đa (Creating).
	 *
	 * @param theta user’s latent ability / năng lực của người dùng
	 * @return ELO points needed to advance to the next level
	 */
	public int eloToNextLevel(double theta) {
		if (calculateMasteryLevel(theta) >= 6) {
			return 0;
		}
		int eloInLevel = thetaToEloInLevel(theta);
		return ELO_PER_LEVEL - eloInLevel;
	}

	public static class UserAnswerGenerator {

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
						.correct(correct)
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

	/**
	 * Reviews a recent set of answers to update theta and generate the next optimal
	 * difficulty range.
	 *
	 * <p>
	 * [EN] Processes a batch of newly submitted answers, appends them to history,
	 * calculates the new ability score,
	 * and predicts the optimal difficulty boundaries for the next assessment round.
	 * <p>
	 * [VI] Tiếp nhận các câu trả lời mới, cập nhật lại điểm năng lực Theta, và tính
	 * toán ra khoảng độ khó (b_min, b_max)
	 * phù hợp nhất để chuẩn bị cho câu hỏi tiếp theo.
	 *
	 * @param answers      list of newly submitted answers / danh sách các câu trả
	 *                     lời mới nộp
	 * @param thetaCurrent current user ability before update / điểm năng lực hiện
	 *                     tại trước khi cập nhật
	 * @param history      accumulated history of all past answers / lịch sử toàn bộ
	 *                     các câu trả lời trước đây
	 * @return array containing [newTheta, suggested_b_min, suggested_b_max] / mảng
	 *         chứa Theta mới và khoảng độ khó gợi ý
	 */
	public double[] reviewAnswer(List<UserAnswer> answers, double thetaCurrent, List<UserAnswer> history) {
		double sigma = 1.0;
		double dampingFactor = DEFAULT_DAMPING_FACTOR;
		double b_min = 0;
		double b_max = 0;
		final double B_MIN_LIMIT = -3.0;
		final double B_MAX_LIMIT = 3.0;

		for (int i = 0; i < answers.size(); i++) {

			// [EN] 1. Suggest difficulty based on P(target)
			// [VI] 1. Gợi ý độ khó theo P(target)
			double pTarget = 0.8;
			double suggestedB = suggestDifficultyB(thetaCurrent, pTarget);

			suggestedB = Math.max(suggestedB, B_MIN_LIMIT);
			suggestedB = Math.min(suggestedB, B_MAX_LIMIT);

			// [EN] 2. Create the proposed difficulty range (B_min, B_max)
			// [VI] 2. Tạo khoảng B đề xuất (B_min, B_max)
			b_min = Math.max(suggestedB - 0.3, B_MIN_LIMIT);
			b_max = Math.min(suggestedB + 0.3, B_MAX_LIMIT);

			// [EN] 3. Get current user answer
			// [VI] 3. Lấy dữ liệu người dùng hiện tại
			UserAnswer userAnswer = answers.get(i);

			// [EN] 4. Append to history
			// [VI] 4. Ghi vào lịch sử
			history.add(userAnswer);

			// [EN] 5. Calculate new theta (MAP)
			// [VI] 5. Tính theta mới (bằng phương pháp MAP)
			double newTheta = estimateThetaMAP(history, thetaCurrent, sigma, dampingFactor);

			// [EN] 6. Update current theta for the next iteration
			// [VI] 6. Cập nhật current theta cho vòng lặp tiếp theo
			thetaCurrent = newTheta;
		}

		return new double[] { thetaCurrent, b_min, b_max };
	}

}
