package com.example.CineBook.controller;

import com.example.CineBook.common.config.VNPAYConfig;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.common.util.VNPayUtil;
import com.example.CineBook.dto.payment.PaymentResponse;
import com.example.CineBook.dto.payment.QRPaymentResponse;
import com.example.CineBook.dto.payment.RefundRequest;
import com.example.CineBook.dto.payment.RefundResponse;
import com.example.CineBook.dto.payment.VNPayCallbackResponse;
import com.example.CineBook.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs quản lý thanh toán")
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPAYConfig vnpayConfig;

    @GetMapping("/vn-pay")
    @Operation(summary = "Tạo URL thanh toán VNPay")
    public ResponseEntity<ApiResponse<PaymentResponse>> createVNPayPayment(HttpServletRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Create VNPay payment url successfully",
                        paymentService.createVNPayPayment(request))
        );
    }

    @PostMapping("/vn-pay-qr/{bookingId}")
    @Operation(summary = "Tạo QR code thanh toán VNPay",
               description = "Trả về QR code để khách quét bằng app ngân hàng")
    public ResponseEntity<ApiResponse<QRPaymentResponse>> createVNPayQRPayment(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(
                ApiResponse.success("Create VNPay QR payment successfully",
                        paymentService.createVNPayQRPayment(bookingId))
        );
    }

    @GetMapping("/vn-pay-callback")
    @Operation(summary = "Xử lý callback từ VNPay sau khi thanh toán")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> payCallbackHandler(HttpServletRequest request) {
        // 1. Get signature from VNPay
        String vnpSecureHash = request.getParameter("vnp_SecureHash");

        // 2. Build params map from ALL request parameters (except vnp_SecureHash and vnp_SecureHashType)
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!key.equals("vnp_SecureHash")) {
                params.put(key, values[0]);
            }
        });

        // 3. Calculate expected signature using your secret key
        String hashData = VNPayUtil.getPaymentURL(params, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData);

        // 4. Compare signatures
        if (!vnpSecureHash.equals(calculatedHash)) {
            // Signature mismatch - possible fraud!
            throw new BusinessException(MessageCode.INVALID_SIGNATURE);
        }
        
        // 5. Process payment (signature verified!)
        String status = request.getParameter("vnp_ResponseCode");
        String bookingIdParam = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String queryString = request.getQueryString();

        if (status.equals("00")) {
            try {
                paymentService.handleVNPayCallback(UUID.fromString(bookingIdParam), status, transactionNo);
            } catch (Exception e) {
                log.error("Error processing payment callback", e);
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/booking/success?" + queryString))
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/booking/failed?" + queryString))
                    .build();
        }
    }

    // 2. DÀNH CHO IPN URL (Cấu hình trên Portal VNPAY Sandbox như bạn vừa hỏi)
    @GetMapping("/vn-pay-ipn")
    public ResponseEntity<Map<String, String>> payIpnHandler(HttpServletRequest request) {
        // 1. Get signature from VNPay
        String vnpSecureHash = request.getParameter("vnp_SecureHash");

        // 2. Build params map from ALL request parameters (except vnp_SecureHash and vnp_SecureHashType)
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!key.equals("vnp_SecureHash")) {
                params.put(key, values[0]);
            }
        });

        // 3. Calculate expected signature using your secret key
        String hashData = VNPayUtil.getPaymentURL(params, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData);
        boolean validSignature = vnpSecureHash.equals(calculatedHash);

        // Nếu sai chữ ký
        if (!validSignature) {
            Map<String, String> response = new HashMap<>();
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return ResponseEntity.ok(response);
        }

        String status = request.getParameter("vnp_ResponseCode");
        String bookingIdParam = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");

        try {
            // THỰC HIỆN LOGIC LƯU DB, NHẢ GHẾ Ở ĐÂY
            paymentService.handleVNPayCallback(UUID.fromString(bookingIdParam), status, transactionNo);

            // TRẢ VỀ ĐÚNG FORMAT NÀY THÌ VNPAY MỚI DỪNG GỌI LẠI
            Map<String, String> response = new HashMap<>();
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("RspCode", "99");
            response.put("Message", "Unknown Error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/bookings/{bookingId}/refund")
    @Operation(summary = "Hoàn tiền cho booking", 
               description = "Chỉ có thể hoàn tiền cho booking đã thanh toán và trước 24h suất chiếu")
    public ResponseEntity<ApiResponse<RefundResponse>> refundBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody RefundRequest request) {
        RefundResponse response = paymentService.refundBooking(bookingId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }
}
