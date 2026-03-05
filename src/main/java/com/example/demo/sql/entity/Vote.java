package com.example.demo.sql.entity;

import com.example.demo.dto.basemodel.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Entity for tracking community votes (Upvotes/Downvotes).
 * 
 * <p>
 * Lưu trữ lịch sử tương tác của người dùng với các bài thảo luận (Form).
 * Tránh việc một người có thể vote nhiều lần trên cùng một nội dung.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "vote_data")
public class Vote extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form targetPost;

    /**
     * 1 for Upvote, -1 for Downvote.
     */
    private int value;
}
