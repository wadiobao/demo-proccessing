package com.example.demo.modules.document.upload.domain.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.springframework.stereotype.Service;

import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.HandleException;

/**
 * Domain Service chịu trách nhiệm kiểm tra nội dung PDF để phát hiện mã độc
 * và các mẫu tấn công phổ biến trước khi file được xử lý bởi hệ thống.
 *
 * <p>Các mối đe dọa được bảo vệ:
 * <ul>
 *   <li>MIME Type Spoofing — giả mạo Content-Type header</li>
 *   <li>PDF Bomb — file nhỏ nhưng expand cực lớn khi parse</li>
 *   <li>JavaScript Injection — nhúng JS vào document/page actions</li>
 *   <li>Path Traversal — filename chứa ký tự nguy hiểm</li>
 * </ul>
 */
@Service
public class PdfSafetyDomainService {

    // PDF magic bytes: %PDF-
    private static final byte[] PDF_MAGIC = { 0x25, 0x50, 0x44, 0x46, 0x2D };

    // Giới hạn số trang — phòng PDF bomb dạng page explosion
    private static final int MAX_PAGE_COUNT = 500;

    // Ký tự nguy hiểm trong filename: path traversal, null byte, shell injection
    private static final Pattern DANGEROUS_FILENAME_PATTERN =
            Pattern.compile(".*(\\.\\.|[/\\\\<>:\"|?*\u0000]).*");

    /**
     * Thực hiện toàn bộ chuỗi kiểm tra an toàn cho file PDF.
     * Ném {@link HandleException} ngay khi phát hiện vi phạm đầu tiên.
     *
     * @param inputStream luồng dữ liệu file cần kiểm tra (chưa được đọc)
     * @param filename    tên file gốc do client gửi lên
     */
    public void verify(InputStream inputStream, String filename) {
        verifyFilename(filename);
        byte[] fileBytes = readBytes(inputStream);
        verifyMagicBytes(fileBytes);
        verifyPdfContent(fileBytes);
    }

    /**
     * Kiểm tra tên file không chứa ký tự path traversal hoặc shell injection.
     *
     * @param filename tên file gốc từ request
     */
    private void verifyFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new HandleException(ErrorCode.MALICIOUS_FILE);
        }
        if (DANGEROUS_FILENAME_PATTERN.matcher(filename).matches()) {
            throw new HandleException(ErrorCode.MALICIOUS_FILE);
        }
    }

    /**
     * Xác nhận 5 bytes đầu khớp với PDF magic signature {@code %PDF-}.
     * Tránh bị lừa bởi Content-Type header giả mạo.
     *
     * @param fileBytes nội dung file dạng byte array
     */
    private void verifyMagicBytes(byte[] fileBytes) {
        if (fileBytes.length < PDF_MAGIC.length) {
            throw new HandleException(ErrorCode.MALICIOUS_FILE);
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (fileBytes[i] != PDF_MAGIC[i]) {
                throw new HandleException(ErrorCode.MALICIOUS_FILE);
            }
        }
    }

    /**
     * Dùng PDFBox để parse và kiểm tra nội dung PDF:
     * <ul>
     *   <li>Cấu trúc PDF phải hợp lệ (không corrupt)</li>
     *   <li>Số trang không vượt quá {@value #MAX_PAGE_COUNT}</li>
     *   <li>Không có JavaScript nhúng ở document-level hoặc page-level action</li>
     * </ul>
     *
     * @param fileBytes nội dung file dạng byte array
     */
    private void verifyPdfContent(byte[] fileBytes) {
        try (PDDocument doc = PDDocument.load(fileBytes)) {

            // Phòng PDF bomb dạng page explosion
            if (doc.getNumberOfPages() > MAX_PAGE_COUNT) {
                throw new HandleException(ErrorCode.MALICIOUS_FILE);
            }

            // Kiểm tra JavaScript ở document-level (OpenAction, AA)
            if (hasDocumentLevelJavaScript(doc)) {
                throw new HandleException(ErrorCode.MALICIOUS_FILE);
            }

            // Kiểm tra JavaScript nhúng trong từng trang
            for (PDPage page : doc.getPages()) {
                if (hasPageLevelJavaScript(page)) {
                    throw new HandleException(ErrorCode.MALICIOUS_FILE);
                }
            }

        } catch (HandleException e) {
            // Re-throw để không bị nuốt bởi catch bên dưới
            throw e;
        } catch (IOException e) {
            // PDF bị corrupt hoặc không parse được → reject
            throw new HandleException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * Kiểm tra xem document có chứa JavaScript ở cấp document (OpenAction, AA dictionary) không.
     *
     * @param doc tài liệu PDF đã được parse
     */
    private boolean hasDocumentLevelJavaScript(PDDocument doc) {
        // Kiểm tra /OpenAction
        COSBase openAction = doc.getDocumentCatalog().getCOSObject()
                .getDictionaryObject(COSName.getPDFName("OpenAction"));
        if (isJavaScriptAction(openAction)) {
            return true;
        }

        // Kiểm tra /AA (Additional Actions) ở catalog
        COSBase aa = doc.getDocumentCatalog().getCOSObject()
                .getDictionaryObject(COSName.getPDFName("AA"));
        if (aa instanceof COSDictionary aaDic) {
            for (COSBase val : aaDic.getValues()) {
                if (isJavaScriptAction(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem một trang PDF có chứa JavaScript trong /AA (Additional Actions) không.
     *
     * @param page trang PDF cần kiểm tra
     */
    private boolean hasPageLevelJavaScript(PDPage page) {
        COSBase aa = page.getCOSObject().getDictionaryObject(COSName.getPDFName("AA"));
        if (aa instanceof COSDictionary aaDic) {
            for (COSBase val : aaDic.getValues()) {
                if (isJavaScriptAction(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra một {@link COSBase} entry có phải là JavaScript action không.
     * Nhận diện bằng {@code /S /JavaScript} trong dictionary.
     *
     * @param base đối tượng COS cần kiểm tra
     */
    private boolean isJavaScriptAction(COSBase base) {
        if (!(base instanceof COSDictionary dic)) {
            return false;
        }
        COSBase subtype = dic.getDictionaryObject(COSName.S);
        return COSName.getPDFName("JavaScript").equals(subtype);
    }

    /**
     * Đọc toàn bộ nội dung InputStream vào byte array.
     * Ném {@link HandleException} nếu đọc thất bại.
     *
     * @param inputStream luồng dữ liệu file
     */
    private byte[] readBytes(InputStream inputStream) {
        try {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new HandleException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
