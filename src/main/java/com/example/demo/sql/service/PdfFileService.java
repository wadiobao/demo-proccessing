package com.example.demo.sql.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.sql.dto.PdfFileFilterRequest;
import com.example.demo.sql.dto.PdfFileRequest;
import com.example.demo.sql.entity.Major;
import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.repository.MajorRepository;
import com.example.demo.sql.repository.PdfFileRepository;
import com.example.demo.sql.service.iservice.IPdfFileService;
import com.example.demo.utils.CloudinaryUtils;

import jakarta.transaction.Transactional;

@Service
public class PdfFileService implements IPdfFileService {

    @Autowired
    private PdfFileRepository pdfFileRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CloudinaryUtils cloudinaryUtils;

    @Override
    public Page<PdfFile> getAllPdfs(Pageable pageable) {
        return pdfFileRepository.findAll(pageable);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(String cloudinaryId) {
        try {
            PdfFile file = pdfFileRepository.findByCloudinaryId(cloudinaryId).orElseThrow();
            pdfFileRepository.delete(file);
            return true;
        } catch (Exception e) {
            // Log error
        }
        return false;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteChecked(List<String> cloudinaryId) {
        try {
            List<PdfFile> file = pdfFileRepository.findAllByCloudinaryIdIn(cloudinaryId);
            cloudinaryUtils.delete(cloudinaryId);
            pdfFileRepository.deleteAll(file);
            return true;
        } catch (Exception e) {
            // Log error
        }
        return false;
    }

    @Override
    public Page<PdfFile> findAllByMajor(PdfFileFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getNumPage(), request.getSize());
        if (request.getMajorId() != null) {
            return pdfFileRepository.findAllByMajorId(request.getMajorId(), pageable);
        }
        return pdfFileRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public PdfFile uploadPdf(MultipartFile file, PdfFileRequest request) throws java.io.IOException {
        Map<String, String> uploadResult = cloudinaryUtils.uploadPdf(file);
        
        Major major = null;
        if (request.getMajorId() != null) {
            major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new RuntimeException("Major not found with id: " + request.getMajorId()));
        }

        PdfFile pdfFile = PdfFile.builder()
                .title(request.getTitle() != null ? request.getTitle() : file.getOriginalFilename())
                .pdfUrl(uploadResult.get("secure_url"))
                .cloudinaryId(uploadResult.get("public_id"))
                .major(major)
                .fileType(request.getFileType())
                .author(request.getAuthor())
                .build();
        
        return pdfFileRepository.save(pdfFile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PdfFile updatePdf(Long id, com.example.demo.sql.dto.PdfFileRequest request) {
        PdfFile pdfFile = pdfFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PdfFile not found with id: " + id));
        
        if (request.getTitle() != null) {
            pdfFile.setTitle(request.getTitle());
        }
        if (request.getMajorId() != null) {
            Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new RuntimeException("Major not found with id: " + request.getMajorId()));
            pdfFile.setMajor(major);
        }
        if (request.getFileType() != null) {
            pdfFile.setFileType(request.getFileType());
        }
        if (request.getAuthor() != null) {
            pdfFile.setAuthor(request.getAuthor());
        }
        
        return pdfFileRepository.save(pdfFile);
    }
}
