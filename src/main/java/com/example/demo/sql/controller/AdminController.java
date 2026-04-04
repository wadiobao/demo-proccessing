package com.example.demo.sql.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.sql.service.iservice.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * AdminController provides administrative operations for managing the system.
 *
 * <p>All operations in this controller require the ADMIN role.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    IUserService userService;
    com.example.demo.sql.service.iservice.IPdfFileService pdfFileService;
    com.example.demo.sql.service.iservice.ICommentService commentService;
    com.example.demo.mongo.service.iservice.IArchivedQuestionService archivedQuestionService;
    com.example.demo.mongo.service.QuestionBankService questionBankService;

    /**
     * Retrieves a paginated list of all users.
     *
     * @param page the page number to retrieve
     * @param size the size of the page
     * @return a paginated list of users
     */
    @GetMapping("/users")
    public ResponseEntity<StateResponse<Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(StateResponse.builder()
                .result(userService.getAll(PageRequest.of(page, size)))
                .build());
    }

//    /**
//     * Updates the roles of a specific user.
//     *
//     * @param userId the ID of the user to update
//     * @param roles the new set of roles
//     * @return a success message
//     */
//    @PutMapping("/users/{id}/role")
//    public ResponseEntity<StateResponse<Object>> updateUserRole(
//            @PathVariable("id") Long userId,
//            @RequestBody Set<String> roles) {
//        userService.updateRole(userId, roles);
//        return ResponseEntity.ok(StateResponse.builder()
//                .message("User role updated successfully")
//                .build());
//    }

    /**
     * Deletes a user from the system.
     *
     * @param userId the ID of the user to delete
     * @return a success message
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<StateResponse<Object>> deleteUser(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(StateResponse.builder()
                .message("User deleted successfully")
                .build());
    }

    /**
     * Retrieves a paginated list of all PDF files.
     *
     * @param page the page number to retrieve
     * @param size the size of the page
     * @return a paginated list of PDF files
     */
    @GetMapping("/files")
    public ResponseEntity<StateResponse<Object>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(StateResponse.builder()
                .result(pdfFileService.getAllPdfs(PageRequest.of(page, size)))
                .build());
    }

    /**
     * Deletes a PDF file by its Cloudinary ID.
     *
     * @param cloudinaryId the Cloudinary ID of the file to delete
     * @return a success message or error
     */
    @DeleteMapping("/files/{cloudinaryId}")
    public ResponseEntity<StateResponse<Object>> deleteFile(@PathVariable("cloudinaryId") String cloudinaryId) {
        boolean deleted = pdfFileService.delete(cloudinaryId);
        if (deleted) {
            return ResponseEntity.ok(StateResponse.builder()
                    .message("File deleted successfully")
                    .build());
        }
        return ResponseEntity.status(400).body(StateResponse.builder()
                .message("Failed to delete file")
                .build());
    }

    /**
     * Deletes multiple PDF files by their Cloudinary IDs.
     *
     * @param cloudinaryIds the list of Cloudinary IDs to delete
     * @return a success message or error
     */
    @DeleteMapping("/files/bulk")
    public ResponseEntity<StateResponse<Object>> deleteFiles(@RequestBody java.util.List<String> cloudinaryIds) {
        boolean deleted = pdfFileService.deleteChecked(cloudinaryIds);
        if (deleted) {
            return ResponseEntity.ok(StateResponse.builder()
                    .message("Files deleted successfully")
                    .build());
        }
        return ResponseEntity.status(400).body(StateResponse.builder()
                .message("Failed to delete files")
                .build());
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId the ID of the comment to delete
     * @return a success message
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<StateResponse<Object>> deleteComment(@PathVariable("id") String commentId) {
        return ResponseEntity.ok(commentService.deleteComment(commentId));
    }

    /**
     * Retrieves all archived question sets.
     *
     * @return a list of archived questions
     */
    @GetMapping("/questions/archived")
    public ResponseEntity<StateResponse<Object>> getAllArchivedQuestions() {
        return ResponseEntity.ok(StateResponse.builder()
                .result(archivedQuestionService.findAll())
                .build());
    }

    /**
     * Deletes an archived question set (last one for the given author).
     *
     * @param author the username of the author whose archived question set should be deleted
     * @return a success message
     * @throws Exception if an error occurs during deletion
     */
    @DeleteMapping("/questions/archived/{author}")
    public ResponseEntity<StateResponse<Object>> deleteArchivedQuestion(@PathVariable("author") String author) throws Exception {
        archivedQuestionService.delete(author);
        return ResponseEntity.ok(StateResponse.builder()
                .message("Archived question set for " + author + " deleted successfully")
                .build());
    }

    /**
     * Retrieves a paginated list of all questions in the bank.
     *
     * @param page the page number to retrieve
     * @param size the size of the page
     * @return a paginated list of questions
     */
    @GetMapping("/questions")
    public ResponseEntity<StateResponse<Object>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(StateResponse.builder()
                .result(questionBankService.searchQuestions("", PageRequest.of(page, size)))
                .build());
    }

    /**
     * Promotes all questions related to a content ID to VERIFIED status.
     *
     * @param contentId the ID of the content to promote
     * @return a success message
     */
    @PutMapping("/questions/promote/{contentId}")
    public ResponseEntity<StateResponse<Object>> promoteQuestions(@PathVariable("contentId") String contentId) {
        questionBankService.promoteByContentId(contentId);
        return ResponseEntity.ok(StateResponse.builder()
                .message("Questions for content " + contentId + " promoted successfully")
                .build());
    }
}
