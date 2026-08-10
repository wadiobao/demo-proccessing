package com.example.demo.modules.document.metadata.infrastructure.adapter.output;

import com.example.demo.modules.document.metadata.application.port.output.EmbeddingPort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DjlEmbeddingAdapter implements EmbeddingPort {

    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        log.info("Khởi tạo DjlEmbeddingAdapter (Sử dụng Langchain4j AllMiniLmL6V2)");
        try {
            // Khởi tạo model nhúng chạy hoàn toàn offline bằng ONNX
            this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
            log.info("Model AllMiniLmL6V2 đã được tải thành công.");
        } catch (Exception e) {
            log.error("Không thể khởi tạo AllMiniLmL6V2EmbeddingModel: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Double> embedDocument(String text) {
        log.info("Đang tạo vector (embedding) cho đoạn văn bản...");
        if (this.embeddingModel == null) {
            log.warn("Model chưa được khởi tạo, trả về vector rỗng!");
            return new ArrayList<>();
        }
        
        try {
            // Gọi thư viện để chuyển text thành vector
            Embedding embedding = this.embeddingModel.embed(text).content();
            
            // Chuyển đổi từ float[] sang List<Double> để tương thích với cấu trúc hiện tại
            float[] vectorArray = embedding.vector();
            List<Double> doubleVector = new ArrayList<>(vectorArray.length);
            for (float v : vectorArray) {
                doubleVector.add((double) v);
            }
            log.info("Tạo vector thành công (Số chiều: {})", doubleVector.size());
            return doubleVector;
        } catch (Exception e) {
            log.error("Lỗi khi tạo embedding: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
