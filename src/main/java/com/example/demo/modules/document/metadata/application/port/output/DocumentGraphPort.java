package com.example.demo.modules.document.metadata.application.port.output;

import java.util.List;
import java.util.Set;

public interface DocumentGraphPort {
    void addDocumentToGraph(String docId, List<Double> embedding, List<String> tags, Set<String> entities);
    List<String> getNeighbors(String docId, int limit);
}
