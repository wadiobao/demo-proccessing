package com.example.demo.modules.document.metadata.application.port.output;

import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import java.util.List;

public interface DocumentClusteringPort {
    // Trả về danh sách chủ đề hoặc map gán nhãn
    void clusterDocuments(List<DocumentMetadata> allDocs);
}
