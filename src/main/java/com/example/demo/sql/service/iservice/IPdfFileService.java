package com.example.demo.sql.service.iservice;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.sql.dto.PdfFileFilterRequest;
import com.example.demo.sql.entity.PdfFile;

public interface IPdfFileService {
    Page<PdfFile> getAllPdfs(Pageable pageable);

    boolean delete(String cloudinaryId);

    boolean deleteChecked(List<String> cloudinaryId);

    Page<PdfFile> findAllByMajor(PdfFileFilterRequest request);
}
