package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.tutorPayment.TutorPaymentResponse;
import com.linked.classbridge.service.TutorPaymentService;
import com.linked.classbridge.type.ResponseMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorPaymentController {

    private final TutorPaymentService tutorPaymentService;

    @GetMapping("/payments")
    public ResponseEntity<SuccessResponse<List<TutorPaymentResponse>>> getTutorPayments(@RequestParam Long userId) {
        List<TutorPaymentResponse> tutorPaymentResponseList = tutorPaymentService.getTutorPaymentsByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.TUTOR_PAYMENT_GET_SUCCESS,
                        tutorPaymentResponseList
                )
        );
    }
}
