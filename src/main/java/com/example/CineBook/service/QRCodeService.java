package com.example.CineBook.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class QRCodeService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Generate QR code as Base64 string
     * @param content Content to encode (ticket code)
     * @return Base64 encoded PNG image
     */
    public String generateQRCodeBase64(String content) {
        try {
            // Paragraph 1: Generate QR code BitMatrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

            // Paragraph 2: Convert BitMatrix to PNG and encode to Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Paragraph 3: Get byte array and encode to Base64
            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);
            
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
//<=========================================== Note: ===============================================>
//QRCodeWriter là class của thư viện ZXing.
//encode(...):
    //Nhận content
    //Kiểu mã: QR_CODE
    //Kích thước ảnh

//Trả về BitMatrix → ma trận các ô đen/trắng của QR Code.
//ByteArrayOutputStream: stream ghi dữ liệu vào RAM (mảng byte)
//writeToStream(...):
//    Chuyển BitMatrix → ảnh PNG
//    Ghi ảnh đó vào outputStream

//Lấy toàn bộ bytes của ảnh PNG
//Encode sang Base64
//Trả về chuỗi Base64
//→ Dùng để nhúng vào email / HTML:
//<img src="data:image/png;base64,{{qrBase64}}">

    /**
     * Generate QR code as byte array
     * @param content Content to encode
     * @return PNG image bytes
     */
    public byte[] generateQRCodeBytes(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
            
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
