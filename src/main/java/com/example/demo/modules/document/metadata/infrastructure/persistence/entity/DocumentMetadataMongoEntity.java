package com.example.demo.modules.document.metadata.infrastructure.persistence.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.dto.basemodel.BaseModel;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * MongoDB Entity for storing document semantic metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "content")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentMetadataMongoEntity extends BaseModel {
    @Id
    String id;
    String content;
    String owner;
    List<String> tags;
    List<Double> embedding;
    String topic;
    String originalName;

    @Transient
    Double vectorSearchScore;

    public DocumentMetadata toDomain() {
        return DocumentMetadata.builder()
                .id(this.id)
                .content(this.content)
                .owner(this.owner)
                .tags(this.tags)
                .embedding(this.embedding)
                .topic(this.topic)
                .originalName(this.originalName)
                .vectorSearchScore(this.vectorSearchScore)
                .build();
    }

    public static DocumentMetadataMongoEntity fromDomain(DocumentMetadata domain) {
        return DocumentMetadataMongoEntity.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .owner(domain.getOwner())
                .tags(domain.getTags())
                .embedding(domain.getEmbedding())
                .topic(domain.getTopic())
                .originalName(domain.getOriginalName())
                .build();
    }
}
