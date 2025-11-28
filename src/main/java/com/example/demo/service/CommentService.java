package com.example.demo.service;

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.form.CommentRequest;
import com.example.demo.dto.form.CommentResponse;
import com.example.demo.service.iservice.ICommentService;
import com.example.demo.sql.entity.Comment;
import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.CommentRepository;
import com.example.demo.sql.repository.FormRepository;
import com.example.demo.sql.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService{
	CommentRepository commentRepository;
	UserRepository userRepository;
	FormRepository  formRepository;

	@Override
	@Transactional
	public StateResponse<Object> newComment(String formId, CommentRequest request) {
		var context = SecurityContextHolder.getContext();
		String name = context.getAuthentication().getName();

		User user = userRepository.findByUserName(name).orElseThrow();
		
		Form form = formRepository.findById(formId).orElseThrow();

		Comment comment = Comment.builder().user(user).noiDung(request.getNoiDung()).form(form)
				.ngayComment(new Date()).build();
		commentRepository.save(comment);
		
		return StateResponse.builder().result(
				CommentResponse.builder().tacGia(name).noiDung(comment.getNoiDung()).ngayComment(comment.getNgayComment()).build()
				).build();
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public StateResponse<Object> deleteComment(String commentId) {
		Long id = Long.parseLong(commentId);
		commentRepository.deleteById(id);
		if(commentRepository.findById(id).isEmpty()) {
			return StateResponse.builder().message("Xóa thành công").build();
		}	
		
		return StateResponse.builder().message("Xóa không thành công").build();
	}
}
