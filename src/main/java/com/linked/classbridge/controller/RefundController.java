package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.refund.PaymentRefundDto;
import com.linked.classbridge.service.KakaoRefundService;
import com.linked.classbridge.type.ResponseMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refunds")
public class RefundController {
    private final KakaoRefundService refundService;
    /**
     * 환불
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<PaymentRefundDto.Response>> processRefund(@RequestBody PaymentRefundDto.Requset requset,
                                                Authentication authentication) {

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REFUND_SUCCESS,
                        refundService.refundPayment(requset, authentication)
                )
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PaymentRefundDto>>> getAllRefunds() {

        List<PaymentRefundDto> refunds = refundService.getAllRefundsByUser();

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REFUND_GET_SUCCESS,
                        refunds
                )
        );
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<SuccessResponse<PaymentRefundDto>> getRefundById(@PathVariable Long refundId) {
        PaymentRefundDto refund = refundService.getRefundById(refundId);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.REFUND_GET_SUCCESS,
                        refund
                )
        );
    }

}
