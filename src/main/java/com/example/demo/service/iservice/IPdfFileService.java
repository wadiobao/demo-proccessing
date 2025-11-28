package com.example.demo.service.iservice;

import java.util.List;

import com.example.demo.dto.PdfFileFilterRequest;
import com.example.demo.sql.entity.PdfFile;

public interface IPdfFileService {
    List<PdfFile> getAllPdfs();

    boolean delete(String cloudinaryId);

    boolean deleteChecked(List<String> cloudinaryId);

	List<PdfFile> findAllByMajor(PdfFileFilterRequest request);
}
