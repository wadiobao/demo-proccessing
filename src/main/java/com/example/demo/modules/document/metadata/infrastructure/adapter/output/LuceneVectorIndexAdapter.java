package com.example.demo.modules.document.metadata.infrastructure.adapter.output;

import com.example.demo.modules.document.metadata.application.port.output.VectorIndexPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LuceneVectorIndexAdapter implements VectorIndexPort {

    private Directory directory;
    private IndexWriter writer;

    @PostConstruct
    public void init() {
        log.info("Khởi tạo LuceneVectorIndexAdapter (Apache Lucene KNN)");
        try {
            // Lưu index vào thư mục cục bộ (có thể đổi thành ByteBuffersDirectory nếu muốn lưu RAM)
            this.directory = FSDirectory.open(Paths.get("lucene_vector_index"));
            IndexWriterConfig config = new IndexWriterConfig();
            this.writer = new IndexWriter(directory, config);
        } catch (IOException e) {
            log.error("Lỗi khi khởi tạo Lucene Index Writer: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (directory != null) {
                directory.close();
            }
        } catch (IOException e) {
            log.error("Lỗi khi đóng Lucene Index: {}", e.getMessage());
        }
    }

    @Override
    public void indexDocument(String docId, List<Double> embedding, Map<String, String> metadata) {
        log.info("Indexing vector cho tài liệu {} vào Lucene...", docId);
        if (writer == null || embedding == null || embedding.isEmpty()) return;

        try {
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i).floatValue();
            }

            Document luceneDoc = new Document();
            luceneDoc.add(new StringField("doc_id", docId, Field.Store.YES));
            luceneDoc.add(new KnnFloatVectorField("embedding", vector, VectorSimilarityFunction.COSINE));
            
            if (metadata != null) {
                metadata.forEach((k, v) -> luceneDoc.add(new StringField(k, v, Field.Store.YES)));
            }

            // Dùng updateDocument để ghi đè nếu docId đã tồn tại
            writer.updateDocument(new Term("doc_id", docId), luceneDoc);
            writer.commit();
            log.info("Lưu vector thành công cho doc_id: {}", docId);
        } catch (IOException e) {
            log.error("Lỗi khi lưu document vào Lucene: {}", e.getMessage());
        }
    }

    @Override
    public List<String> findNearest(List<Double> queryEmbedding, int limit) {
        log.info("Tìm kiếm {} hàng xóm gần nhất qua Lucene KNN...", limit);
        List<String> results = new ArrayList<>();
        if (writer == null || queryEmbedding == null || queryEmbedding.isEmpty()) return results;

        try {
            float[] queryVector = new float[queryEmbedding.size()];
            for (int i = 0; i < queryEmbedding.size(); i++) {
                queryVector[i] = queryEmbedding.get(i).floatValue();
            }

            try (DirectoryReader reader = DirectoryReader.open(writer)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                KnnFloatVectorQuery knnQuery = new KnnFloatVectorQuery("embedding", queryVector, limit);
                TopDocs topDocs = searcher.search(knnQuery, limit);

                for (ScoreDoc sd : topDocs.scoreDocs) {
                    Document d = searcher.storedFields().document(sd.doc);
                    results.add(d.get("doc_id"));
                    log.info("Tìm thấy neighbor: doc_id={} với độ tương đồng={}", d.get("doc_id"), sd.score);
                }
            }
        } catch (IOException e) {
            log.error("Lỗi khi tìm kiếm KNN trong Lucene: {}", e.getMessage());
        }
        return results;
    }
}
