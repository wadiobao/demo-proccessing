package com.example.demo.modules.document.metadata.infrastructure.adapter.output;

import com.example.demo.modules.document.metadata.application.port.output.DocumentClusteringPort;
import com.example.demo.modules.document.metadata.domain.model.DocumentMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import smile.clustering.DBSCAN;
import java.util.List;

@Component
@Slf4j
public class SmileClusteringAdapter implements DocumentClusteringPort {

    public SmileClusteringAdapter() {
        log.info("Khởi tạo SmileClusteringAdapter (DBSCAN)");
    }

    @Override
    public void clusterDocuments(List<DocumentMetadata> allDocs) {
        if (allDocs == null || allDocs.size() < 3) {
            log.info("Không đủ dữ liệu để phân cụm (yêu cầu >= 3). Tổng số tài liệu: {}",
                    allDocs == null ? 0 : allDocs.size());
            return;
        }

        log.info("Bắt đầu phân cụm {} tài liệu bằng DBSCAN...", allDocs.size());

        try {
            // Chuẩn bị ma trận vector
            double[][] data = new double[allDocs.size()][];
            for (int i = 0; i < allDocs.size(); i++) {
                List<Double> embedding = allDocs.get(i).getEmbedding();
                if (embedding != null) {
                    double[] vector = new double[embedding.size()];
                    for (int j = 0; j < embedding.size(); j++) {
                        vector[j] = embedding.get(j);
                    }
                    data[i] = vector;
                } else {
                    data[i] = new double[0];
                }
            }

            // minPts (số lượng hàng xóm tối thiểu)
            int minPts = Math.min(5, allDocs.size() / 2);
            if (minPts < 2) {
                minPts = 2; // minPts tối thiểu của DBSCAN là 2
            }
            double eps = 0.5; // Bán kính lân cận (có thể cần điều chỉnh tùy thuộc vào vector)

            // Thực thi DBSCAN
            DBSCAN<double[]> dbscan = DBSCAN.fit(data, minPts, eps);

            // Lấy nhãn cụm (-1 là noise/outlier)
            int[] labels = dbscan.y;
            int numClusters = -1;

            for (int i = 0; i < allDocs.size(); i++) {
                int clusterId = labels[i];
                if (clusterId > numClusters) {
                    numClusters = clusterId;
                }

                DocumentMetadata doc = allDocs.get(i);

                // Gán tạm log để kiểm tra
                if (clusterId != -1) {
                    log.debug("Tài liệu '{}' được gán vào cụm {}", doc.getOriginalName(), clusterId);
                } else {
                    log.debug("Tài liệu '{}' được phân loại là Outlier (Nhiễu)", doc.getOriginalName());
                }
            }

            log.info("Phân cụm hoàn tất. Tìm thấy {} cụm hợp lệ.", numClusters + 1);

        } catch (Exception e) {
            log.error("Lỗi khi chạy HDBSCAN phân cụm tài liệu: {}", e.getMessage());
        }
    }
}
