package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.StateResponse;
import com.example.demo.dto.form.CommentResponse;
import com.example.demo.dto.form.FormRequest;
import com.example.demo.dto.form.FormResponse;
import com.example.demo.dto.form.TopicRequest;
import com.example.demo.dto.form.TopicResponse;
import com.example.demo.sql.entity.Comment;
import com.example.demo.sql.entity.Form;
import com.example.demo.sql.entity.FormContent;
import com.example.demo.sql.entity.Topic;
import com.example.demo.sql.entity.User;
import com.example.demo.sql.repository.CommentRepository;
import com.example.demo.sql.repository.FormContentRepository;
import com.example.demo.sql.repository.FormRepository;
import com.example.demo.sql.repository.TopicRepository;

@ExtendWith(MockitoExtension.class)
class FormServiceTest {

    @Mock
    private FormRepository formRepository;

    @Mock
    private FormContentRepository contentRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private FormService formService;

    private Form form;
    private FormContent formContent;
    private Topic topic;
    private Comment comment;

    @BeforeEach
    void setUp() {
        topic = Topic.builder().topicId(1L).topic("Test Topic").build();
        formContent = FormContent.builder().noiDung("Test Content").build();
        form = Form.builder()
                .formId("form123")
                .tacGia("testuser")
                .tieuDe("Test Form Title")
                .tags(new HashSet<>(Arrays.asList("tag1", "tag2")))
                .ngayDang(new Date())
                .topic(topic)
                .content(formContent)
                .build();
        comment = Comment.builder()
                .commenttId(1L)
                .user(User.builder().userName("commenter").build())
                .noiDung("Test Comment")
                .ngayComment(new Date())
                .build();

        // Mock SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllForm_ShouldReturnListOfFormResponses() {
        when(formRepository.findAllByOrderByNgayDangDesc()).thenReturn(Arrays.asList(form));

        StateResponse<Object> response = formService.getAllForm();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof List);
        List<FormResponse> formResponses = (List<FormResponse>) response.getResult();
        assertEquals(1, formResponses.size());
        assertEquals(form.getFormId(), formResponses.get(0).getFormId());
        verify(formRepository, times(1)).findAllByOrderByNgayDangDesc();
    }

    @Test
    void newForm_ValidRequest_ShouldReturnFormResponse() {
        FormRequest formRequest = FormRequest.builder()
                .tieuDe("New Form Title")
                .tags("newtag1,newtag2")
                .content("New Form Content")
                .build();

        when(topicRepository.findById(anyLong())).thenReturn(Optional.of(topic));
        when(contentRepository.save(any(FormContent.class))).thenReturn(formContent);
        when(formRepository.save(any(Form.class))).thenReturn(form);

        StateResponse<Object> response = formService.newForm(1L, formRequest);

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof FormResponse);
        FormResponse formResponse = (FormResponse) response.getResult();
        assertEquals("New Form Title", formResponse.getTieuDe());
        verify(topicRepository, times(1)).findById(anyLong());
        verify(contentRepository, times(1)).save(any(FormContent.class));
        verify(formRepository, times(1)).save(any(Form.class));
    }

    @Test
    void newForm_TopicNotFound_ShouldThrowException() {
        FormRequest formRequest = FormRequest.builder()
                .tieuDe("New Form Title")
                .tags("newtag1,newtag2")
                .content("New Form Content")
                .build();

        when(topicRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> formService.newForm(1L, formRequest));
        verify(topicRepository, times(1)).findById(anyLong());
        verify(contentRepository, never()).save(any(FormContent.class));
        verify(formRepository, never()).save(any(Form.class));
    }

    @Test
    void getFormComment_ShouldReturnListOfCommentResponses() {
        when(commentRepository.findByForm_FormId(anyString())).thenReturn(Arrays.asList(comment));

        StateResponse<Object> response = formService.getFormComment("form123");

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof List);
        List<CommentResponse> commentResponses = (List<CommentResponse>) response.getResult();
        assertEquals(1, commentResponses.size());
        assertEquals(comment.getNoiDung(), commentResponses.get(0).getNoiDung());
        verify(commentRepository, times(1)).findByForm_FormId(anyString());
    }

    @Test
    void newTopic_ValidRequest_ShouldReturnTopic() {
        TopicRequest topicRequest = TopicRequest.builder().topic("New Topic").build();
        when(topicRepository.save(any(Topic.class))).thenReturn(topic);

        StateResponse<Object> response = formService.newTopic(topicRequest);

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof Topic);
        Topic returnedTopic = (Topic) response.getResult();
        assertEquals("New Topic", returnedTopic.getTopic());
        verify(topicRepository, times(1)).save(any(Topic.class));
    }

    @Test
    void getAllTopic_ShouldReturnListOfTopicResponsesWithForms() {
        topic.setForm(Arrays.asList(form)); // Set forms for the topic
        when(topicRepository.findAll()).thenReturn(Arrays.asList(topic));

        StateResponse<Object> response = formService.getAllTopic();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof List);
        List<TopicResponse> topicResponses = (List<TopicResponse>) response.getResult();
        assertEquals(1, topicResponses.size());
        assertEquals(topic.getTopic(), topicResponses.get(0).getTopic());
        assertEquals(1, topicResponses.get(0).getForms().size());
        verify(topicRepository, times(1)).findAll();
    }

    @Test
    void getAllTopics_ShouldReturnListOfTopicResponsesWithoutForms() {
        when(topicRepository.findAll()).thenReturn(Arrays.asList(topic));

        StateResponse<Object> response = formService.getAllTopics();

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof List);
        List<TopicResponse> topicResponses = (List<TopicResponse>) response.getResult();
        assertEquals(1, topicResponses.size());
        assertEquals(topic.getTopic(), topicResponses.get(0).getTopic());
        assertNull(topicResponses.get(0).getForms()); // Should not contain forms
        verify(topicRepository, times(1)).findAll();
    }

    @Test
    void getAllFormFromTopic_ShouldReturnListOfFormResponses() {
        when(formRepository.findByTopic_TopicIdOrderByNgayDangDesc(anyLong())).thenReturn(Arrays.asList(form));

        StateResponse<Object> response = formService.getAllFormFromTopic(1L);

        assertNotNull(response);
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof List);
        List<FormResponse> formResponses = (List<FormResponse>) response.getResult();
        assertEquals(1, formResponses.size());
        assertEquals(form.getFormId(), formResponses.get(0).getFormId());
        verify(formRepository, times(1)).findByTopic_TopicIdOrderByNgayDangDesc(anyLong());
    }

    @Test
    void deleteForm_ShouldReturnSuccessMessage() {
        doNothing().when(formRepository).deleteById(anyString());
        doNothing().when(contentRepository).deleteById(anyString());
        doNothing().when(commentRepository).deleteAllByFormId(anyString());

        StateResponse<Object> response = formService.deleteForm("form123");

        assertNotNull(response);
        assertEquals("Xóa thành công form", response.getMessage());
        verify(formRepository, times(1)).deleteById(anyString());
        verify(contentRepository, times(1)).deleteById(anyString());
        verify(commentRepository, times(1)).deleteAllByFormId(anyString());
    }
}
