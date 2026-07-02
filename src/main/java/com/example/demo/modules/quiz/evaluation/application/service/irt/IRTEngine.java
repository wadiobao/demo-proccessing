package com.example.demo.modules.quiz.evaluation.application.service.irt;

import java.util.List;
import com.example.demo.modules.quiz.shared.domain.model.UserAnswer;
import org.springframework.stereotype.Component;

/**
 * Mathematical engine for estimating user ability and adjusting question
 * difficulty in adaptive assessments.
 *
 * <p>
 * [EN] This class implements the Maximum A Posteriori (MAP) estimation using
 * the 1-Parameter Logistic (1PL/Rasch), 2-Parameter Logistic (2PL), and
 * 3-Parameter Logistic (3PL) Item Response Theory (IRT) models.
 * <p>
 * [VI] Lớp thực thi thuật toán Cảm ứng Câu hỏi (IRT) hỗ trợ các mô hình
 * 1-tham số (1PL/Rasch), 2-tham số (2PL), và 3-tham số (3PL).
 */
@Component
public class IRTEngine {

	/**
	 * Tính toán xác suất trả lời đúng của người dùng dựa trên mô hình IRT 3 tham số (3PL).
	 *
	 * <p>
	 * [EN] Computes the probability of a correct answer using the 3-Parameter Logistic (3PL) IRT model.
	 * <p>
	 * [VI] Tính toán xác suất trả lời đúng của người dùng dựa trên mô hình IRT 3 tham số (3PL).
	 *
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @param a     [EN] Item discrimination | [VI] Độ phân biệt của câu hỏi
	 * @param c     [EN] Pseudo-guessing probability | [VI] Xác suất đoán mò ngẫu nhiên
	 * @return [EN] Probability of correct answer [0.0, 1.0] | [VI] Xác suất trả lời đúng câu hỏi [0.0, 1.0]
	 */
	public double p3PL(double theta, double b, double a, double c) {
		double expTerm = Math.exp(-a * (theta - b));
		return c + (1.0 - c) / (1.0 + expTerm);
	}

	/**
	 * Tính toán xác suất trả lời đúng của người dùng dựa trên mô hình IRT 1 tham số (1PL / Rasch).
	 *
	 * <p>
	 * [EN] Computes the probability of a correct answer using the 1-Parameter Logistic (1PL / Rasch) IRT model.
	 * <p>
	 * [VI] Tính toán xác suất trả lời đúng của người dùng dựa trên mô hình IRT 1 tham số (1PL / Rasch).
	 *
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @return [EN] Probability of correct answer [0.0, 1.0] | [VI] Xác suất trả lời đúng câu hỏi [0.0, 1.0]
	 */
	public double p(double theta, double b) {
		return p3PL(theta, b, 1.0, 0.0);
	}

	/**
	 * Tính toán giá trị Likelihood (hợp lý) cho một phản hồi cụ thể của người dùng trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the likelihood of a specific response (correct/incorrect) under the 3PL model.
	 * <p>
	 * [VI] Tính toán giá trị Likelihood (hợp lý) cho một phản hồi cụ thể của người dùng trong mô hình 3PL.
	 *
	 * @param x     [EN] Actual response (1 for correct, 0 for incorrect) | [VI] Kết quả trả lời thực tế (1 nếu đúng, 0 nếu sai)
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @param a     [EN] Item discrimination | [VI] Độ phân biệt của câu hỏi
	 * @param c     [EN] Pseudo-guessing probability | [VI] Xác suất đoán mò ngẫu nhiên
	 * @return [EN] Likelihood value [0.0, 1.0] | [VI] Giá trị khả năng (likelihood) của phản hồi [0.0, 1.0]
	 */
	public double likelihood3PL(int x, double theta, double b, double a, double c) {
		double p = p3PL(theta, b, a, c);
		return x == 1 ? p : (1 - p);
	}

	/**
	 * Tính toán giá trị Likelihood cho một phản hồi cụ thể trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the likelihood of a specific response under the 1PL model.
	 * <p>
	 * [VI] Tính toán giá trị Likelihood cho một phản hồi cụ thể trong mô hình 1PL.
	 *
	 * @param x     [EN] Actual response (1 for correct, 0 for incorrect) | [VI] Kết quả trả lời thực tế (1 nếu đúng, 0 nếu sai)
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @return [EN] Likelihood value [0.0, 1.0] | [VI] Giá trị khả năng (likelihood) của phản hồi [0.0, 1.0]
	 */
	public double likelihood(int x, double theta, double b) {
		return likelihood3PL(x, theta, b, 1.0, 0.0);
	}

	/**
	 * Tính toán giá trị log tự nhiên của hàm Likelihood (Log-Likelihood) cho một phản hồi đơn lẻ trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the natural logarithm of the likelihood function for a single response under the 3PL model.
	 * <p>
	 * [VI] Tính toán giá trị log tự nhiên của hàm Likelihood (Log-Likelihood) cho một phản hồi đơn lẻ trong mô hình 3PL.
	 *
	 * @param x     [EN] Actual response (1 for correct, 0 for incorrect) | [VI] Kết quả trả lời thực tế (1 nếu đúng, 0 nếu sai)
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @param a     [EN] Item discrimination | [VI] Độ phân biệt của câu hỏi
	 * @param c     [EN] Pseudo-guessing probability | [VI] Xác suất đoán mò ngẫu nhiên
	 * @return [EN] Log-Likelihood value | [VI] Giá trị Log-Likelihood của phản hồi
	 */
	public double logLikelihood3PL(int x, double theta, double b, double a, double c) {
		double p = p3PL(theta, b, a, c);
		return x * Math.log(p) + (1 - x) * Math.log(1 - p);
	}

	/**
	 * Tính toán giá trị Log-Likelihood cho một phản hồi đơn lẻ trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the natural logarithm of the likelihood function for a single response under the 1PL model.
	 * <p>
	 * [VI] Tính toán giá trị Log-Likelihood cho một phản hồi đơn lẻ trong mô hình 1PL.
	 *
	 * @param x     [EN] Actual response (1 for correct, 0 for incorrect) | [VI] Kết quả trả lời thực tế (1 nếu đúng, 0 nếu sai)
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @return [EN] Log-Likelihood value | [VI] Giá trị Log-Likelihood của phản hồi
	 */
	public double logLikelihood(int x, double theta, double b) {
		return logLikelihood3PL(x, theta, b, 1.0, 0.0);
	}

	/**
	 * Tính toán tổng Log-Likelihood tích lũy từ danh sách các câu trả lời của người dùng trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the aggregate log-likelihood of a sequence of responses under the 3PL model.
	 * <p>
	 * [VI] Tính toán tổng Log-Likelihood tích lũy từ danh sách các câu trả lời của người dùng trong mô hình 3PL.
	 *
	 * @param answers  [EN] List of user answers | [VI] Danh sách các câu trả lời của người dùng
	 * @param theta    [EN] Latent ability under estimation | [VI] Năng lực tiềm ẩn giả định của người dùng
	 * @param defaultA [EN] Default item discrimination | [VI] Độ phân biệt mặc định
	 * @param defaultC [EN] Default pseudo-guessing value | [VI] Xác suất đoán mò mặc định
	 * @return [EN] Sum of log-likelihoods | [VI] Tổng giá trị Log-Likelihood trên toàn bộ câu trả lời
	 */
	public double totalLogLikelihood3PL(List<UserAnswer> answers, double theta, double defaultA, double defaultC) {
		double sum = 0;
		for (UserAnswer a : answers) {
			double itemA = defaultA;
			double itemC = defaultC;
			double p = p3PL(theta, a.getDifficulty(), itemA, itemC);
			sum += a.isCorrect() ? Math.log(p) : Math.log(1 - p);
		}
		return sum;
	}

	/**
	 * Tính toán tổng Log-Likelihood tích lũy từ danh sách các câu trả lời của người dùng trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the aggregate log-likelihood of a sequence of responses under the 1PL model.
	 * <p>
	 * [VI] Tính toán tổng Log-Likelihood tích lũy từ danh sách các câu trả lời của người dùng trong mô hình 1PL.
	 *
	 * @param answers [EN] List of user answers | [VI] Danh sách các câu trả lời của người dùng
	 * @param theta   [EN] Latent ability under estimation | [VI] Năng lực tiềm ẩn giả định của người dùng
	 * @return [EN] Sum of log-likelihoods | [VI] Tổng giá trị Log-Likelihood trên toàn bộ câu trả lời
	 */
	public double totalLogLikelihood(List<UserAnswer> answers, double theta) {
		return totalLogLikelihood3PL(answers, theta, 1.0, 0.0);
	}

	/**
	 * Tính toán đạo hàm bậc nhất (Gradient) của phân phối Log-Posterior theo theta trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the first derivative (Gradient) of the Log-Posterior distribution with respect to theta under the 3PL model, incorporating a Gaussian prior penalty.
	 * <p>
	 * [VI] Tính toán đạo hàm bậc nhất (Gradient) của phân phối Log-Posterior theo theta trong mô hình 3PL, kết hợp với hàm tiền định Gaussian.
	 *
	 * @param answers  [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param theta    [EN] Current latent ability value | [VI] Giá trị năng lực hiện tại của người dùng
	 * @param sigma    [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @param defaultA [EN] Default item discrimination | [VI] Độ phân biệt mặc định
	 * @param defaultC [EN] Default pseudo-guessing value | [VI] Xác suất đoán mò mặc định
	 * @return [EN] Gradient value at theta | [VI] Giá trị Gradient tại điểm theta
	 */
	public double gradientMAP3PL(List<UserAnswer> answers, double theta, double sigma, double defaultA, double defaultC) {
		double g = 0;
		for (UserAnswer a : answers) {
			double itemA = defaultA;
			double itemC = defaultC;
			double p = p3PL(theta, a.getDifficulty(), itemA, itemC);
			double score = a.isCorrect() ? 1.0 : 0.0;
			
			double num = itemA * (p - itemC) * (score - p);
			double den = (1.0 - itemC) * p;
			if (Math.abs(den) > 1e-9) {
				g += num / den;
			}
		}
		g -= theta / (sigma * sigma);
		return g;
	}

	/**
	 * Tính toán đạo hàm bậc nhất (Gradient) của phân phối Log-Posterior theo theta trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the first derivative (Gradient) of the Log-Posterior distribution under the 1PL model.
	 * <p>
	 * [VI] Tính toán đạo hàm bậc nhất (Gradient) của phân phối Log-Posterior theo theta trong mô hình 1PL.
	 *
	 * @param answers [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param theta   [EN] Current latent ability value | [VI] Giá trị năng lực hiện tại của người dùng
	 * @param sigma   [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @return [EN] Gradient value at theta | [VI] Giá trị Gradient tại điểm theta
	 */
	public double gradientMAP(List<UserAnswer> answers, double theta, double sigma) {
		return gradientMAP3PL(answers, theta, sigma, 1.0, 0.0);
	}

	/**
	 * Tính toán đạo hàm bậc hai (Hessian) của phân phối Log-Posterior theo theta trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the second derivative (Hessian) of the Log-Posterior distribution with respect to theta under the 3PL model, incorporating a Gaussian prior penalty.
	 * <p>
	 * [VI] Tính toán đạo hàm bậc hai (Hessian) của phân phối Log-Posterior theo theta trong mô hình 3PL, kết hợp với hàm tiền định Gaussian.
	 *
	 * @param answers  [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param theta    [EN] Current latent ability value | [VI] Giá trị năng lực hiện tại của người dùng
	 * @param sigma    [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @param defaultA [EN] Default item discrimination | [VI] Độ phân biệt mặc định
	 * @param defaultC [EN] Default pseudo-guessing value | [VI] Xác suất đoán mò mặc định
	 * @return [EN] Hessian value at theta | [VI] Giá trị Hessian tại điểm theta
	 */
	public double hessianMAP3PL(List<UserAnswer> answers, double theta, double sigma, double defaultA, double defaultC) {
		double h = 0;
		for (UserAnswer a : answers) {
			double itemA = defaultA;
			double itemC = defaultC;
			double p = p3PL(theta, a.getDifficulty(), itemA, itemC);
			double score = a.isCorrect() ? 1.0 : 0.0;
			
			double pPrime = itemA * (p - itemC) * (1.0 - p) / (1.0 - itemC);
			double num = pPrime * pPrime * (score - p);
			double den1 = p * p * (1.0 - p) * (1.0 - p);
			double term1 = 0;
			if (Math.abs(den1) > 1e-9) {
				term1 = num / den1; 
			}
			
			double den2 = p * (1.0 - p);
			double term2 = 0;
			if (Math.abs(den2) > 1e-9) {
				term2 = (pPrime * pPrime) / den2;
			}
			h += (term1 - term2);
		}
		h -= 1.0 / (sigma * sigma);
		return h;
	}

	/**
	 * Tính toán đạo hàm bậc hai (Hessian) của phân phối Log-Posterior theo theta trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the second derivative (Hessian) of the Log-Posterior distribution under the 1PL model.
	 * <p>
	 * [VI] Tính toán đạo hàm bậc hai (Hessian) của phân phối Log-Posterior theo theta trong mô hình 1PL.
	 *
	 * @param answers [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param theta   [EN] Current latent ability value | [VI] Giá trị năng lực hiện tại của người dùng
	 * @param sigma   [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @return [EN] Hessian value at theta | [VI] Giá trị Hessian tại điểm theta
	 */
	public double hessianMAP(List<UserAnswer> answers, double theta, double sigma) {
		return hessianMAP3PL(answers, theta, sigma, 1.0, 0.0);
	}

	/**
	 * Ước lượng năng lực tiềm ẩn của người dùng (Theta) bằng phương pháp tối ưu hóa Maximum A Posteriori (MAP) dưới mô hình 3PL.
	 *
	 * <p>
	 * [EN] Estimates the user's latent ability (Theta) using Maximum A Posteriori (MAP) optimization under the 3PL model via Newton-Raphson iteration.
	 * <p>
	 * [VI] Ước lượng năng lực tiềm ẩn của người dùng (Theta) bằng phương pháp tối ưu hóa Maximum A Posteriori (MAP) dưới mô hình 3PL thông qua thuật toán lặp Newton-Raphson.
	 *
	 * @param answers       [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param thetaInit     [EN] Initial theta guess | [VI] Giá trị khởi tạo cho năng lực theta
	 * @param sigma         [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @param dampingFactor [EN] Step size damping factor | [VI] Hệ số cản để kiểm soát bước cập nhật
	 * @param defaultA      [EN] Default item discrimination | [VI] Độ phân biệt mặc định
	 * @param defaultC      [EN] Default pseudo-guessing value | [VI] Xác suất đoán mò mặc định
	 * @return [EN] Estimated theta ability | [VI] Giá trị năng lực theta tối ưu sau hội tụ
	 */
	public double estimateThetaMAP3PL(List<UserAnswer> answers, double thetaInit, double sigma, double dampingFactor, double defaultA, double defaultC) {
		double theta = thetaInit;
		double epsilon = 1e-6;
		int maxIter = 100;

		for (int iter = 0; iter < maxIter; iter++) {
			double g = gradientMAP3PL(answers, theta, sigma, defaultA, defaultC);
			double h = hessianMAP3PL(answers, theta, sigma, defaultA, defaultC);
			
			if (Math.abs(h) < 1e-9) {
				h = -1e-9;
			}
			
			double step = g / h;
			double dampened_step = dampingFactor * step;
			theta -= dampened_step;
			theta = Math.max(Math.min(theta, 3.0), -3.0);
			if (Math.abs(dampened_step) < epsilon) {
				break;
			}
		}
		return theta;
	}

	/**
	 * Ước lượng năng lực tiềm ẩn của người dùng (Theta) bằng phương pháp tối ưu hóa MAP dưới mô hình 1PL.
	 *
	 * <p>
	 * [EN] Estimates the user's latent ability (Theta) using MAP optimization under the 1PL model via Newton-Raphson iteration.
	 * <p>
	 * [VI] Ước lượng năng lực tiềm ẩn của người dùng (Theta) bằng phương pháp tối ưu hóa MAP dưới mô hình 1PL thông qua thuật toán lặp Newton-Raphson.
	 *
	 * @param answers       [EN] List of user answers | [VI] Danh sách câu trả lời của người dùng
	 * @param thetaInit     [EN] Initial theta guess | [VI] Giá trị khởi tạo cho năng lực theta
	 * @param sigma         [EN] Standard deviation of Gaussian prior | [VI] Độ lệch chuẩn của phân phối tiền định Gaussian
	 * @param dampingFactor [EN] Step size damping factor | [VI] Hệ số cản để kiểm soát bước cập nhật
	 * @return [EN] Estimated theta ability | [VI] Giá trị năng lực theta tối ưu sau hội tụ
	 */
	public double estimateThetaMAP(List<UserAnswer> answers, double thetaInit, double sigma, double dampingFactor) {
		return estimateThetaMAP3PL(answers, thetaInit, sigma, dampingFactor, 1.0, 0.0);
	}

	/**
	 * Tính toán lượng thông tin Fisher (Fisher Information) của câu hỏi tại điểm năng lực cụ thể trong mô hình 3PL.
	 *
	 * <p>
	 * [EN] Computes the Fisher Information of a question at a given ability level under the 3PL model.
	 * <p>
	 * [VI] Tính toán lượng thông tin Fisher (Fisher Information) của câu hỏi tại điểm năng lực cụ thể trong mô hình 3PL.
	 *
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @param a     [EN] Item discrimination | [VI] Độ phân biệt của câu hỏi
	 * @param c     [EN] Pseudo-guessing probability | [VI] Xác suất đoán mò ngẫu nhiên
	 * @return [EN] Fisher Information value | [VI] Lượng thông tin Fisher của câu hỏi đối với theta
	 */
	public double fisher3PL(double theta, double b, double a, double c) {
		double p = p3PL(theta, b, a, c);
		double num = a * a * (1.0 - p) * (p - c) * (p - c);
		double den = (1.0 - c) * (1.0 - c) * p;
		if (Math.abs(den) > 1e-9) {
			return num / den;
		}
		return 0;
	}

	/**
	 * Tính toán lượng thông tin Fisher của câu hỏi tại điểm năng lực cụ thể trong mô hình 1PL.
	 *
	 * <p>
	 * [EN] Computes the Fisher Information of a question at a given ability level under the 1PL model.
	 * <p>
	 * [VI] Tính toán lượng thông tin Fisher của câu hỏi tại điểm năng lực cụ thể trong mô hình 1PL.
	 *
	 * @param theta [EN] User latent ability | [VI] Năng lực tiềm ẩn của người dùng
	 * @param b     [EN] Item difficulty | [VI] Độ khó của câu hỏi
	 * @return [EN] Fisher Information value | [VI] Lượng thông tin Fisher của câu hỏi đối với theta
	 */
	public double fisher(double theta, double b) {
		return fisher3PL(theta, b, 1.0, 0.0);
	}
}
