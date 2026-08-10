package com.example.demo.modules.document.metadata.application.port.output;

import java.util.List;
import java.util.Map;

public interface VectorIndexPort {
    void indexDocument(String docId, List<Double> embedding, Map<String, String> metadata);
    List<String> findNearest(List<Double> queryEmbedding, int limit);
}
