package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.tutorPayment.TutorPaymentResponse;
import com.linked.classbridge.service.TutorPaymentService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorPaymentController {

    private final TutorPaymentService tutorPaymentService;

    @Operation(summary = "강사 정산 조회", description = "강사 정산 내역을 조회합니다.")
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/payments")
    public ResponseEntity<SuccessResponse<List<TutorPaymentResponse>>> getTutorPayments(
            @RequestParam(required = false) String yearMonth) {
        List<TutorPaymentResponse> tutorPaymentResponseList;
        if (yearMonth != null) {
            YearMonth ym = YearMonth.parse(yearMonth);
            tutorPaymentResponseList = tutorPaymentService.getTutorPaymentsByUserIdAndPeriod(ym);
        } else {
            tutorPaymentResponseList = tutorPaymentService.getTutorPaymentsByUserId();
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.TUTOR_PAYMENT_GET_SUCCESS,
                        tutorPaymentResponseList
                )
        );
    }
}
