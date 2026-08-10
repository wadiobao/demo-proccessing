package com.example.demo.modules.document.metadata.infrastructure.adapter.output;

import com.example.demo.modules.document.metadata.application.port.output.DocumentGraphPort;
import com.example.demo.modules.document.metadata.application.port.output.VectorIndexPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JGraphTDocumentGraphAdapter implements DocumentGraphPort {

    private final VectorIndexPort vectorIndexPort;
    private Graph<String, DefaultWeightedEdge> graph;

    @PostConstruct
    public void init() {
        log.info("Khởi tạo JGraphTDocumentGraphAdapter (In-memory Graph)");
        this.graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    @Override
    public void addDocumentToGraph(String docId, List<Double> embedding, List<String> tags, Set<String> entities) {
        log.info("Đang thêm tài liệu {} vào đồ thị JGraphT...", docId);
        
        // Đảm bảo node tồn tại trong đồ thị
        graph.addVertex(docId);

        // 1. Tìm các hàng xóm gần nhất (Semantic Similarity) qua Lucene
        if (embedding != null && !embedding.isEmpty()) {
            // Lấy 10 hàng xóm gần nhất
            List<String> nearestNeighbors = vectorIndexPort.findNearest(embedding, 10);
            for (String neighborId : nearestNeighbors) {
                if (!neighborId.equals(docId)) {
                    graph.addVertex(neighborId);
                    if (!graph.containsEdge(docId, neighborId)) {
                        // Thêm cạnh với trọng số cơ bản
                        DefaultWeightedEdge edge = graph.addEdge(docId, neighborId);
                        if (edge != null) {
                            graph.setEdgeWeight(edge, 1.0); 
                            log.debug("Thêm liên kết ngữ nghĩa: {} <--> {}", docId, neighborId);
                        }
                    }
                }
            }
        }

        // 2. Xử lý liên kết thực thể (Entity) để tạo kết nối gián tiếp
        if (entities != null) {
            for (String entity : entities) {
                String entityNodeId = "entity:" + entity;
                graph.addVertex(entityNodeId);
                if (!graph.containsEdge(docId, entityNodeId)) {
                    DefaultWeightedEdge edge = graph.addEdge(docId, entityNodeId);
                    if (edge != null) {
                        graph.setEdgeWeight(edge, 0.5); 
                    }
                }
            }
        }
    }

    @Override
    public List<String> getNeighbors(String docId, int limit) {
        if (!graph.containsVertex(docId)) {
            return new ArrayList<>();
        }

        // Trả về các file liên kết trực tiếp, lọc bỏ các Node thực thể (entity:...)
        return graph.edgesOf(docId).stream()
                .map(edge -> {
                    String source = graph.getEdgeSource(edge);
                    String target = graph.getEdgeTarget(edge);
                    return source.equals(docId) ? target : source;
                })
                .filter(node -> !node.startsWith("entity:")) 
                .limit(limit)
                .collect(Collectors.toList());
    }
}
