package com.example.demo.modules.community.forum.application.usecase.command;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.community.forum.api.dto.FormRequest;
import com.example.demo.modules.community.forum.api.dto.FormResponse;
import com.example.demo.modules.community.forum.infrastructure.persistence.entity.Form;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormContentRepository;
import com.example.demo.modules.community.forum.infrastructure.persistence.repository.FormRepository;

import lombok.RequiredArgsConstructor;

/**
 * Cập nhật tiêu đề và nội dung form của tác giả hiện tại.
 *
 * <p>Kiểm tra quyền sở hữu thông qua SecurityContext (so sánh với tacGia).
 * Nếu hợp lệ, cập nhật tieuDe, noiDung và gán cờ hasChanged = true.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UpdateFormUseCase {

    private final FormRepository formRepository;
    private final FormContentRepository formContentRepository;

    /**
     * Cập nhật form dựa trên ID.
     *
     * @param formId ID của form
     * @param request dữ liệu yêu cầu cập nhật (ưu tiên tieuDe và content)
     * @return StateResponse chứa FormResponse cập nhật
     * @throws RuntimeException nếu không tìm thấy form
     * @throws AccessDeniedException nếu người dùng không phải tác giả
     */
    @Transactional
    public StateResponse<Object> execute(String formId, FormRequest request) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        if (!form.getTacGia().equals(currentUsername)) {
            throw new AccessDeniedException("You don't have permission to update this form");
        }

        form.setTieuDe(request.getTieuDe());
        form.setHasChanged(true);
        form.getContent().setNoiDung(request.getContent());

        formContentRepository.save(form.getContent());
        formRepository.save(form);

        return StateResponse.builder()
                .result(FormResponse.builder()
                        .formId(form.getFormId())
                        .tacGia(form.getTacGia())
                        .tieuDe(form.getTieuDe())
                        .tags(form.getTags())
                        .ngayDang(form.getNgayDang())
                        .noiDung(form.getContent().getNoiDung())
                        .topic(form.getTopic().getTopic())
                        .voteScore(form.getVoteScore())
                        .contentId(form.getContentId())
                        .hasQuiz(form.isHasQuiz())
                        .hasChanged(form.getHasChanged())
                        .isAuthor(true)
                        .build())
                .message("Cập nhật form thành công")
                .build();
    }
}
