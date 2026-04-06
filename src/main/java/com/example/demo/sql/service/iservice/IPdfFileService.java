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

    PdfFile uploadPdf(org.springframework.web.multipart.MultipartFile file, com.example.demo.sql.dto.PdfFileRequest request) throws java.io.IOException;

    PdfFile updatePdf(Long id, com.example.demo.sql.dto.PdfFileRequest request);

    PdfFile getById(Long id);
}
