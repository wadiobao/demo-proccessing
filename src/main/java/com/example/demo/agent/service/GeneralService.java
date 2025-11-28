package com.example.demo.agent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.agent.agents.BehavioralAnalysisAgent;
import com.example.demo.agent.agents.ReGenerateAgent;
import com.example.demo.agent.entity.UserAnswer;
import com.example.demo.agent.entity.UserEvaluation;
import com.example.demo.agent.repository.UserEvaluationRepository;
import com.example.demo.dto.StateResponse;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeneralService {
	BehavioralAnalysisAgent analysisAgent = new BehavioralAnalysisAgent();
	ReGenerateAgent reGenerateAgent = new ReGenerateAgent();
	UserEvaluationRepository evaluationRepository;
	
	@Transactional
	public StateResponse<Object> saveAnalysis(UserAnswer userAnswer, String userId) {
		analysisAgent.runAgent(userId);
		UserEvaluation evaluation = analysisAgent.analysis(userAnswer.toString());
		evaluationRepository.save(evaluation);
		//System.out.println(evaluation.getConceptTags().toString());
		return StateResponse.builder().result(true).build();
	}
	
	public List<UserEvaluation> getUserEvaluationByEmailAndTag(String email, List<String> tags) {
		return evaluationRepository.findByEmailAndTagsLike(email, tags);
	}
	
	public List<UserEvaluation> getUserEvaluationByEmail(String email){
		List<UserEvaluation> evaluations = evaluationRepository.findAllByEmail(email);
		if(evaluations.isEmpty()) {
			throw new HandleException(ErrorCode.USER_NOT_EXISTED);
		}
		return evaluations;
	}
	

}
