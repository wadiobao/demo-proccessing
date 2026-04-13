package com.example.demo.sql.service.iservice;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.modules.document.shared.domain.model.PdfFile;
import com.example.demo.modules.document.upload.api.request.PdfFileFilterRequest;
import com.example.demo.modules.document.upload.api.request.PdfFileRequest;



public interface IPdfFileService {
    Page<PdfFile> getAllPdfs(Pageable pageable);

    boolean delete(String cloudinaryId);

    boolean deleteChecked(List<String> cloudinaryId);

    Page<PdfFile> findAllByMajor(PdfFileFilterRequest request);

    PdfFile uploadPdf(org.springframework.web.multipart.MultipartFile file, PdfFileRequest request) throws java.io.IOException;

    PdfFile updatePdf(Long id, PdfFileRequest request);

    PdfFile getById(Long id);
}
