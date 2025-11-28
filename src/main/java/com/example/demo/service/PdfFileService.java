package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PdfFileFilterRequest;
import com.example.demo.service.iservice.IPdfFileService;
import com.example.demo.sql.entity.PdfFile;
import com.example.demo.sql.repository.PdfFileRepository;
import com.example.demo.utils.CloudinaryUtils;

import jakarta.transaction.Transactional;

@Service
public class PdfFileService implements IPdfFileService {

	@Autowired
	private PdfFileRepository pdfFileRepository;
	
	@Autowired
	private CloudinaryUtils cloudinaryUtils;

	public PdfFileService(PdfFileRepository pdfFileRepository) {
		this.pdfFileRepository = pdfFileRepository;
	}

	@Override
	public List<PdfFile> getAllPdfs() {
		return pdfFileRepository.findAll();
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
			// TODO: handle exception
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
			// TODO: handle exception
		}
		return false;
	}
	
	@Override
	public List<PdfFile> findAllByMajor(PdfFileFilterRequest request) {
		
		Pageable pageable = PageRequest.of(request.getNumPage(),request.getSize());
		
		Page<PdfFile> pagePdf = pdfFileRepository.findAllByMajor(request.getMajor(), pageable);
				
		List<PdfFile> listPdf = pagePdf.toList();
		
		return listPdf;
	} 
}
