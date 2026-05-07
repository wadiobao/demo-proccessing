package com.example.demo.modules.community.forum.application.usecase.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.CommentResponse;
import com.example.demo.modules.community.forum.api.dto.FormDetailResponse;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Comment;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.CommentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.reputation.api.ReputationFacade;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;
import com.example.demo.modules.quiz.shared.api.QuizFacade;
import com.example.demo.modules.quiz.shared.domain.model.Question;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFormDetailUseCase {

    private final FormRepository formRepository;
    private final CommentRepository commentRepository;
    private final IUserRepository userRepository;
    private final QuizFacade quizFacade;
    private final ReputationFacade reputationFacade;

    @Transactional(readOnly = true)
    public StateResponse<Object> execute(String formId, Pageable pageable) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);
        FormResponse formResponse = mapToFormResponse(form, currentUser);

        List<Question> questions = new ArrayList<>();
        if (form.isHasQuiz() && form.getContentId() != null) {
            questions = quizFacade.getQuestionsByContentId(form.getContentId());
        }

        Page<Comment> comments = commentRepository.findByForm_FormId(formId, pageable);
        Page<CommentResponse> responses = comments.map(comment -> CommentResponse.builder()
                .id(comment.getCommenttId())
                .tacGia(comment.getUser().getUserName())
                .noiDung(comment.getNoiDung())
                .ngayComment(comment.getNgayComment())
                .build());

        FormDetailResponse detail = FormDetailResponse.builder()
                .form(formResponse)
                .questions(questions)
                .comments(responses)
                .build();

        return StateResponse.builder().result(detail).build();
    }

    private FormResponse mapToFormResponse(Form form, User currentUser) {
        int userVoteValue = reputationFacade.getUserVote(currentUser, form);

        return FormResponse.builder()
                .formId(form.getFormId())
                .tacGia(form.getTacGia())
                .tieuDe(form.getTieuDe())
                .tags(form.getTags())
                .ngayDang(form.getNgayDang())
                .noiDung(form.getContent().getNoiDung())
                .topic(form.getTopic().getTopic())
                .voteScore(form.getVoteScore())
                .userVoteValue(userVoteValue)
                .contentId(form.getContentId())
                .hasQuiz(form.isHasQuiz())
                .build();
    }
}
