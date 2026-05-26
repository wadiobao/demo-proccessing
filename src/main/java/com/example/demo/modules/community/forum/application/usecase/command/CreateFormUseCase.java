package com.example.demo.modules.community.forum.application.usecase.command;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormRequest;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.api.dto.FormSession;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.FormContent;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Topic;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormContentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.TopicRepository;
import com.example.demo.modules.quiz.shared.api.QuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.Question;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateFormUseCase {

    private final FormRepository formRepository;
    private final FormContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final QuizFacade quizFacade;
    private final RedisTemplate<String, String> redisTemplate;
    private final Gson gson;

    private static final String SESSION_KEY_PREFIX = "form_session:";

    @Transactional
    public StateResponse<Object> execute(Long topicId, FormRequest formRequest, String sessionId) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        String contentId = formRequest.getContentId();
        boolean hasQuiz = contentId != null && !contentId.isEmpty();

        if (sessionId != null && !sessionId.isEmpty()) {
            String raw = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
            if (raw != null) {
                FormSession session = gson.fromJson(raw, FormSession.class);
                if (!name.equals(session.getOwnerName())) {
                    log.warn("User {} tried to commit session {} owned by {}", name, sessionId, session.getOwnerName());
                    throw new RuntimeException("Unauthorized: This session belongs to another user.");
                }

                List<Question> questions = session.getQuestions();
                if (questions != null && !questions.isEmpty()) {
                    if (contentId == null || contentId.isEmpty()) {
                        contentId = UUID.randomUUID().toString();
                    }
                    
                    quizFacade.commitStagedQuestions(questions, name, contentId);
                    hasQuiz = true;

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
                .hasChanged(false)
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
                        .hasChanged(false)
                        .isAuthor(true)
                        .build())
                .build();
    }
}
