package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.form.FormRequest;
import com.example.demo.dto.form.TopicRequest;
import com.example.demo.service.iservice.IFormService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/discussion")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FormController {
	
	IFormService formService;
	
	@GetMapping
	private StateResponse<Object> getAllTopic(){
		return formService.getAllTopics();
	}
	
	@GetMapping("/{topicId}/forms")
	private StateResponse<Object> getAllForm(@PathVariable("topicId") Long topicId){
		return formService.getAllFormFromTopic(topicId);
	}
	
	private StateResponse<Object> getAll(){
		return formService.getAllForm();
	}
	
	@PostMapping("/{topicid}/newform")
	private StateResponse<Object> newForm(@PathVariable("topicid") Long topicId,@RequestBody FormRequest formRequest) {
		return formService.newForm(topicId,formRequest);
	}
	
	@GetMapping("/form/{formId}")
	private StateResponse<Object> getFormComment(@PathVariable("formId") String formId){
		return formService.getFormComment(formId);
	}
	
	@PostMapping("/newtopic")
	private StateResponse<Object> newTopic(@RequestBody TopicRequest request){
		return formService.newTopic(request);
	}
	
	@DeleteMapping("/delete/{formid}")
	public StateResponse<Object> deleteComment(@PathVariable("formid") String formid) {
		return formService.deleteForm(formid);
	}
	


}
