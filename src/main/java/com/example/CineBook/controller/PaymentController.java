package com.example.CineBook.controller;

import com.example.CineBook.common.config.VNPAYConfig;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.common.util.VNPayUtil;
import com.example.CineBook.dto.payment.PaymentResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/vn-pay-callback")
    @Operation(summary = "Xử lý callback từ VNPay sau khi thanh toán")
    public ResponseEntity<ApiResponse<VNPayCallbackResponse>> payCallbackHandler(HttpServletRequest request) {
        // 1. Get signature from VNPay
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        log.info("Received vnp_SecureHash: {}", vnpSecureHash);

        // 2. Build params map from ALL request parameters (except vnp_SecureHash and vnp_SecureHashType)
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            log.info("Key: {}, Value: {}", key, values[0]);
            if (!key.equals("vnp_SecureHash")) {
                params.put(key, values[0]);
            }
        });

        // 3. Calculate expected signature using your secret key
        String hashData = VNPayUtil.getPaymentURL(params, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData);
        log.info("Calculated Hash: {}", calculatedHash);

        // 4. Compare signatures
        if (!vnpSecureHash.equals(calculatedHash)) {
            // Signature mismatch - possible fraud!
            throw new BusinessException(MessageCode.INVALID_SIGNATURE);
        }
        
        // 5. Process payment (signature verified!)
        String status = request.getParameter("vnp_ResponseCode");
        String bookingIdParam = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        log.info("Payment status: {}, bookingId: {}, transactionNo: {}", status, bookingIdParam, transactionNo);

        if (status.equals("00")) {
            try {
                log.info("Run this 1");
                log.info("Type of bookingIdParam: {}", bookingIdParam.getClass().getName());
                log.info("Type of bookingI: {}", UUID.fromString(bookingIdParam).getClass().getName());
                UUID bookingId = UUID.fromString(bookingIdParam);
                log.info("Type of bookingId: {}", bookingId.getClass().getName());
                paymentService.handleVNPayCallback(bookingId, status, transactionNo);
                log.info("Payment processed successfully for bookingId: {}", bookingId);
                VNPayCallbackResponse response = VNPayCallbackResponse.builder()
                        .bookingId(bookingId)
                        .status("PAID")
                        .message("Payment successful")
                        .build();

                return ResponseEntity.ok(ApiResponse.success("Payment successful", response));
            } catch (Exception e) {
                log.info("Run this 2");
                return ResponseEntity.badRequest().body(
                        ApiResponse.fail(MessageCode.PAYMENT_ERROR, 99)
                );
            }
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(MessageCode.PAYMENT_ERROR, 99)
            );
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
