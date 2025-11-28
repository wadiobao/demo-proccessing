package com.example.demo.agent.tools;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.agent.entity.UserEvaluation;
import com.example.demo.agent.repository.UserEvaluationRepository;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.ToolContext;

import jakarta.transaction.Transactional;

@Component
public class ToolsForUserEvaluation {

	@Autowired
	private static UserEvaluationRepository userEvaluationRepository;
	
	public Map<String,String> getUserHistoryBehavior(
			@Schema(name = "user email",
			description = "The email of the user for which to retrieve the history behavior of test")
			String email){
				return null;
	}
	
	@Transactional
	@Schema(
	description = "Save the evaluation into database after analyzing the user answer")
	public static Map<String,String> saveUserEvaluation(
			@Schema(name = "user evaluation",
			description = "The evaluation from the user answer")
			UserEvaluation evaluation
			,@Schema(name = "toolContext")
			ToolContext context) {
		if(userEvaluationRepository.save(evaluation) != null) {
			return Map.of("status","success");
		}
		return Map.of("status","pending");
	}
	
	public UserEvaluation setUserEvaluation() {
		return null;
		
	}
	
	public List<UserEvaluation> getUserEvaluation(String email) {
		List<UserEvaluation> userEvaluations = userEvaluationRepository.findAllByEmail(email);
		return userEvaluations;
	}
}
