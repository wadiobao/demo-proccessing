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
import com.example.demo.mongo.service.BulkQuestionUploadService;
import com.example.demo.sql.repository.UserRepository;
import com.example.demo.sql.repository.VoteRepository;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.entity.Vote;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import com.google.gson.Gson;
import com.example.demo.sql.dto.form.FormSession;
import com.example.demo.mongo.dto.question.Question;
import com.example.demo.mongo.entity.QuestionBank;
import java.util.UUID;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j // Added annotation
public class FormService implements IFormService {

	private final FormRepository formRepository;
	private final FormContentRepository contentRepository;
	private final CommentRepository commentRepository;
	private final TopicRepository topicRepository;
	private final UserRepository userRepository;
	private final VoteRepository voteRepository;
	private final BulkQuestionUploadService bulkQuestionUploadService;
	private final RedisTemplate<String, String> redisTemplate;
	private final Gson gson;

	private static final String SESSION_KEY_PREFIX = "form_session:";
	private static final long SESSION_TTL_SECONDS = 900;

	@Override
	public StateResponse<Object> getAllForm(Pageable pageable) {
		Page<Form> all = formRepository.findAllByOrderByNgayDangDesc(pageable);
		return StateResponse.builder().result(decorateWithVotes(all)).build();
	}

	private Page<FormResponse> decorateWithVotes(Page<Form> forms) {
		String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
		User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

		java.util.Map<String, Integer> userVotes = new java.util.HashMap<>();
		if (currentUser != null && !forms.isEmpty()) {
			List<Vote> votes = voteRepository.findAllByVoterAndTargetPostIn(currentUser, forms.getContent());
			for (Vote v : votes) {
				userVotes.put(v.getTargetPost().getFormId(), v.getValue());
			}
		}

		return forms.map(form -> FormResponse.builder()
				.formId(form.getFormId())
				.tacGia(form.getTacGia())
				.tieuDe(form.getTieuDe())
				.tags(form.getTags())
				.ngayDang(form.getNgayDang())
				.noiDung(form.getContent().getNoiDung())
				.topic(form.getTopic().getTopic())
				.voteScore(form.getVoteScore())
				.userVoteValue(userVotes.getOrDefault(form.getFormId(), 0))
				.contentId(form.getContentId())
				.hasQuiz(form.isHasQuiz())
				.build());
	}

	@Override
	@Transactional
	public StateResponse<Object> newForm(Long topicId, FormRequest formRequest, String sessionId) {
		var context = SecurityContextHolder.getContext();
		String name = context.getAuthentication().getName();

		Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new RuntimeException("Topic not found"));

		String contentId = formRequest.getContentId();
		boolean hasQuiz = contentId != null && !contentId.isEmpty();

		// Handle staged quiz if sessionId is provided
		if (sessionId != null && !sessionId.isEmpty()) {
			String raw = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
			if (raw != null) {
				FormSession session = gson.fromJson(raw, FormSession.class);
				// Security Check: Ensure owner matches
				if (!name.equals(session.getOwnerName())) {
					log.warn("User {} tried to commit session {} owned by {}", name, sessionId, session.getOwnerName());
					throw new RuntimeException("Unauthorized: This session belongs to another user.");
				}

				List<Question> questions = session.getQuestions();
				if (questions != null && !questions.isEmpty()) {
					List<QuestionBank> saved = bulkQuestionUploadService.commitStagedQuestions(questions, name);
					contentId = saved.get(0).getContentId();
					hasQuiz = true;

					// Cleanup session immediately
					redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);
					log.info("Committed session {} for user {}. Created contentId: {}", sessionId, name, contentId);
				}
			}
		}

		Set<String> tags = new HashSet<>();
		if (formRequest.getTags() != null) {
			String[] words = formRequest.getTags().split(",");
			for (String tag : words) {
				tags.add(tag.trim());
			}
		}

		Form form = Form.builder()
				.tacGia(name)
				.tieuDe(formRequest.getTieuDe())
				.tags(tags)
				.ngayDang(new Date())
				.topic(topic)
				.contentId(contentId)
				.hasQuiz(hasQuiz)
				.build();

		FormContent content = new FormContent();
		content.setForm(form);
		content.setNoiDung(formRequest.getContent());
		form.setContent(content);

		contentRepository.save(content);
		formRepository.save(form);

		return StateResponse.builder()
				.result(FormResponse.builder()
						.formId(form.getFormId())
						.tacGia(form.getTacGia())
						.tieuDe(form.getTieuDe())
						.tags(form.getTags())
						.ngayDang(form.getNgayDang())
						.noiDung(content.getNoiDung())
						.topic(topic.getTopic())
						.voteScore(0)
						.userVoteValue(0)
						.contentId(contentId)
						.hasQuiz(hasQuiz)
						.build())
				.build();
	}

	@Override
	public String startSession(String username) {
		String sessionId = UUID.randomUUID().toString();
		FormSession session = FormSession.builder()
				.sessionId(sessionId)
				.ownerName(username)
				.createdAt(System.currentTimeMillis())
				.questions(new ArrayList<>())
				.build();

		redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, gson.toJson(session),
				Duration.ofSeconds(SESSION_TTL_SECONDS));

		log.info("Started form creation session {} for user {}", sessionId, username);
		return sessionId;
	}

	@Override
	public void discardSession(String sessionId, String username) {
		String raw = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
		if (raw != null) {
			FormSession session = gson.fromJson(raw, FormSession.class);
			if (username.equals(session.getOwnerName())) {
				redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);
				log.info("Discarded session {} for user {}", sessionId, username);
			}
		}
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
						.topic(topic.getTopic())
						.hasQuiz(form.isHasQuiz())
						.contentId(form.getContentId())
						.build();
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
		return StateResponse.builder().result(decorateWithVotes(all)).build();
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

	@Override
	public StateResponse<Object> searchByKeyword(String keyword, Pageable pageable) {
		Page<Form> results = formRepository.searchByKeyword(keyword, pageable);
		return StateResponse.builder().result(decorateWithVotes(results)).build();
	}
}
