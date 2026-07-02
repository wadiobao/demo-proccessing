package com.example.demo.modules.quiz.archive.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StateResponse;
import com.example.demo.modules.quiz.archive.api.ArchiveFacade;
import com.example.demo.modules.quiz.shared.infrastructure.persistence.entity.ArchivedSessionMongoEntity;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Managing Archived Quizzes.
 */
@RestController
@RequestMapping("/api/v1/quiz/archive")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class QuizArchiveController {

    private final ArchiveFacade archiveFacade;

    /**
     * Archives a quiz.
     */
    @PostMapping
    public ResponseEntity<ArchivedSessionMongoEntity> create(@RequestBody ArchivedSessionMongoEntity archive) {
        return ResponseEntity.ok(archiveFacade.createArchive(archive));
    }

    /**
     * Retrieves history for a specific author.
     */
    @GetMapping("/author")
    public ResponseEntity<StateResponse<Object>> getAuthorHistory(
            @RequestParam String author,
            Authentication authentication) {
        
        return ResponseEntity.ok(archiveFacade.getAuthorHistory(author));
    }

    /**
     * Deletes a specific archive.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            Authentication authentication) {
        
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        archiveFacade.deleteArchive(id, username, isAdmin);
        return ResponseEntity.noContent().build();
    }

    /**
     * Global archive list for admins.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ArchivedSessionMongoEntity>> getAll() {
        return ResponseEntity.ok(archiveFacade.getAllArchives());
    }
}
