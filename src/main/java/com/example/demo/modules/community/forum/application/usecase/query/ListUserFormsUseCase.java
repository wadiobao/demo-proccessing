package com.example.demo.modules.community.forum.application.usecase.query;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;
import com.example.demo.modules.community.reputation.api.ReputationFacade;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.domain.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lấy danh sách các Form do chính người dùng hiện tại đã đăng.
 *
 * <p>Lọc các Form theo tacGia khớp với username của người dùng đang đăng nhập,
 * sắp xếp theo ngày đăng mới nhất.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ListUserFormsUseCase {

    private final FormRepository formRepository;
    private final IUserRepository userRepository;
    private final ReputationFacade reputationFacade;

    /**
     * Thực thi truy vấn danh sách form đã đăng của người dùng hiện tại.
     *
     * @param pageable tham số phân trang
     * @return StateResponse chứa danh sách FormResponse
     */
    @Transactional(readOnly = true)
    public StateResponse<Object> execute(Pageable pageable) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserName(currentUsername).orElse(null);

        Page<Form> userForms = formRepository.findByTacGiaOrderByNgayDangDesc(currentUsername, pageable);

        Map<String, Integer> userVotes = reputationFacade.getVotesForForms(currentUser, userForms.getContent());

        Page<FormResponse> responses = userForms.map(form -> {
            boolean isFormAuthor = currentUser != null && form.getTacGia().equals(currentUser.getUserName());
            return FormResponse.builder()
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
                    .hasChanged(form.getHasChanged())
                    .isAuthor(isFormAuthor)
                    .build();
        });

        return StateResponse.builder().result(responses).build();
    }
}
