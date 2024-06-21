package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.TutorPaymentDetailRepository;
import com.linked.classbridge.repository.TutorPaymentRepository;
import com.linked.classbridge.domain.TutorPayment;
import com.linked.classbridge.domain.TutorPaymentDetail;
import com.linked.classbridge.dto.tutorPayment.TutorPaymentDetailResponse;
import com.linked.classbridge.dto.tutorPayment.TutorPaymentResponse;
import com.linked.classbridge.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorPaymentService {

    private final PaymentRepository paymentRepository;
    private final TutorPaymentRepository tutorPaymentRepository;
    private final TutorPaymentDetailRepository tutorPaymentDetailRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Transactional
    public void processMonthlySettlement() {
        log.info("batch service start");
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = previousMonth.atDay(1);
        LocalDate endDate = previousMonth.atEndOfMonth();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Payment> payments = paymentRepository.findAllByPaymentDateBetweenAndLessonDateBeforeAndStatusCompleted(startDateTime, endDateTime, LocalDate.now());

        payments.stream()
                .collect(Collectors.groupingBy(payment -> payment.getReservation().getLesson().getOneDayClass().getTutor().getUserId()))
                .forEach((tutorId, tutorPayments) -> {
                    int totalAmount = tutorPayments.stream().mapToInt(Payment::getTotalAmount).sum();
                    TutorPayment tutorPayment = TutorPayment.builder()
                            .userId(tutorId)
                            .amount(totalAmount)
                            .paymentDateTime(LocalDateTime.now())
                            .periodStartDate(startDate)
                            .periodEndDate(endDate)
                            .build();
                    tutorPayment = tutorPaymentRepository.save(tutorPayment);

                    for (Payment payment : tutorPayments) {
                        TutorPaymentDetail tutorPaymentDetail = TutorPaymentDetail.builder()
//                                .tutorPaymentId(tutorPayment.getTutorPaymentId())
                                .tutorPayment(tutorPayment)
//                                .paymentId(payment.getPaymentId())
                                .payment(payment)
                                .createdAt(LocalDateTime.now())
                                .build();
                        tutorPaymentDetailRepository.save(tutorPaymentDetail);
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<TutorPaymentResponse> getTutorPaymentsByUserIdAndPeriod(YearMonth yearMonth) {

        User user = getUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TutorPayment> tutorPayments = tutorPaymentRepository.findByUserIdAndPeriodStartDateBetween(user.getUserId(), startDate, endDate)
                .orElse(Collections.emptyList());

        return tutorPayments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private User getUser() {
        String userEmail = userService.getCurrentUserEmail();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("User with email '{}' not found", userEmail);
                    return new RestApiException(USER_NOT_FOUND);
                });
    }

    @Transactional(readOnly = true)
    public List<TutorPaymentResponse> getTutorPaymentsByUserId() {
        User user = getUser();
        List<TutorPayment> tutorPayments = tutorPaymentRepository.findByUserId(user.getUserId())
                .orElse(Collections.emptyList());
        return tutorPayments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TutorPaymentResponse convertToDto(TutorPayment tutorPayment) {
        List<TutorPaymentDetail> details = tutorPaymentDetailRepository.findByTutorPayment(tutorPayment)
                .orElse(Collections.emptyList());
        List<TutorPaymentDetailResponse> detailResponses = details.stream()
                .map(TutorPaymentDetailResponse::new)
                .collect(Collectors.toList());
        return new TutorPaymentResponse(tutorPayment, detailResponses);
    }
}
