package com.example.demo.sql.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.dto.form.CommentResponse;
import com.example.demo.sql.dto.form.FormRequest;
import com.example.demo.sql.dto.form.FormResponse;
import com.example.demo.sql.dto.form.TopicRequest;
import com.example.demo.sql.dto.form.TopicResponse;
import com.example.demo.sql.service.iservice.IFormService;
import com.example.demo.sql.entity.Comment;
import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.FormContent;
import com.example.demo.sql.entity.Topic;
import com.example.demo.sql.repository.CommentRepository;
import com.example.demo.sql.repository.FormContentRepository;
import com.example.demo.sql.repository.FormRepository;
import com.example.demo.sql.repository.TopicRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FormService implements IFormService {

	FormRepository formRepository;
	FormContentRepository contentRepository;
	CommentRepository commentRepository;
	TopicRepository topicRepository;

	@Override
	public StateResponse<Object> getAllForm(Pageable pageable) {
		Page<Form> all = formRepository.findAllByOrderByNgayDangDesc(pageable);

		Page<FormResponse> responses = all.map(form -> FormResponse.builder()
				.formId(form.getFormId())
				.tacGia(form.getTacGia())
				.tieuDe(form.getTieuDe())
				.tags(form.getTags())
				.ngayDang(form.getNgayDang())
				.noiDung(form.getContent().getNoiDung())
				.topic(form.getTopic().getTopic())
				.build());

		return StateResponse.builder().result(responses).build();
	}

	@Override
	@Transactional
	public StateResponse<Object> newForm(Long topicId, FormRequest formRequest) {
		var contenxt = SecurityContextHolder.getContext();
		String name = contenxt.getAuthentication().getName();

		Set<String> tags = new HashSet<String>();
		String[] words = formRequest.getTags().split(",");
		for (String tag : words) {
			tags.add(tag.trim());
		}
		Topic topic = topicRepository.findById(topicId).orElseThrow();

		Form form = Form.builder().tacGia(name).tieuDe(formRequest.getTieuDe()).tags(tags).ngayDang(new Date())
				.topic(topic).build();

		FormContent content = new FormContent();
		content.setForm(form);
		content.setNoiDung(formRequest.getContent());

		form.setContent(content);

		contentRepository.save(content);
		formRepository.save(form);

		return StateResponse
				.builder()
				.result(FormResponse.builder().formId(form.getFormId()).tacGia(form.getTacGia())
						.tieuDe(form.getTieuDe())
						.tags(form.getTags()).ngayDang(form.getNgayDang()).noiDung(content.getNoiDung())
						.topic(topic.getTopic()).build())
				.build();

	}

	@Override
	@Transactional
	public StateResponse<Object> getFormComment(String formId, Pageable pageable) {
		Page<Comment> comments = commentRepository.findByForm_FormId(formId, pageable);
		Page<CommentResponse> responses = comments.map(comment -> CommentResponse.builder()
				.id(comment.getCommenttId())
				.tacGia(comment.getUser().getUserName())
				.noiDung(comment.getNoiDung())
				.ngayComment(comment.getNgayComment())
				.build());
		return StateResponse.builder().result(responses).build();
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public StateResponse<Object> newTopic(TopicRequest request) {
		Topic topic = Topic.builder().topic(request.getTopic()).form(new ArrayList<Form>()).build();
		topicRepository.save(topic);
		return StateResponse.builder().result(topic).build();
	}

	@Override
	public StateResponse<Object> getAllTopic(Pageable pageable) {
		Page<Topic> topics = topicRepository.findAll(pageable);
		Page<TopicResponse> responses = topics.map(topic -> {
			List<Form> forms = topic.getForm();
			List<FormResponse> formResponses = new ArrayList<FormResponse>();
			for (Form form : forms) {
				FormResponse formResponse = FormResponse.builder().formId(form.getFormId()).tacGia(form.getTacGia())
						.tieuDe(form.getTieuDe())
						.tags(form.getTags()).ngayDang(form.getNgayDang()).noiDung(form.getContent().getNoiDung())
						.topic(topic.getTopic()).build();
				formResponses.add(formResponse);
			}
			return TopicResponse.builder().topicId(topic.getTopicId()).topic(topic.getTopic()).forms(formResponses)
					.build();
		});
		return StateResponse.builder().result(responses).build();
	}

	@Override
	public StateResponse<Object> getAllTopics(Pageable pageable) {
		Page<Topic> topics = topicRepository.findAll(pageable);
		Page<TopicResponse> responses = topics
				.map(topic -> TopicResponse.builder().topicId(topic.getTopicId()).topic(topic.getTopic()).build());
		return StateResponse.builder().result(responses).build();
	}

	@Override
	public StateResponse<Object> getAllFormFromTopic(Long topicId, Pageable pageable) {
		Page<Form> all = formRepository.findByTopic_TopicIdOrderByNgayDangDesc(topicId, pageable);
		Page<FormResponse> responses = all.map(form -> FormResponse.builder()
				.formId(form.getFormId())
				.tacGia(form.getTacGia())
				.tieuDe(form.getTieuDe())
				.tags(form.getTags())
				.ngayDang(form.getNgayDang())
				.noiDung(form.getContent().getNoiDung())
				.topic(form.getTopic().getTopic())
				.build());

		return StateResponse.builder().result(responses).build();
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public StateResponse<Object> deleteForm(String formId) {
		formRepository.deleteById(formId);
		contentRepository.deleteById(formId);
		commentRepository.deleteAllByFormId(formId);

		return StateResponse.builder().message("Xóa thành công form").build();
	}
}
