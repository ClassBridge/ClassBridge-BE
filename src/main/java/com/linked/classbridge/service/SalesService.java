package com.linked.classbridge.service;

import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.TutorPayment;
import com.linked.classbridge.dto.sales.MonthlySales;
import com.linked.classbridge.dto.sales.TutorSalesResponse;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.TutorPaymentRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final TutorPaymentRepository tutorPaymentRepository;
    private final PaymentRepository paymentRepository;


//    public TutorSalesResponse getSalesData(Long tutorId, int year) {
//        YearMonth startMonth = YearMonth.of(year, 1);
//        YearMonth endMonth = YearMonth.of(year, 12);
//
//        List<Sales> salesDataList = salesDataRepository.findByTutorIdAndMonthBetween(tutorId, startMonth, endMonth).orElse(
//                Collections.emptyList());
//
//        List<MonthlySales> monthlySales = salesDataList.stream()
//                .map(salesData -> new MonthlySales(salesData.getMonth().getMonthValue(), salesData.getAmount()))
//                .collect(Collectors.toList());
//
//        int totalSales = monthlySales.stream().mapToInt(MonthlySales::getAmount).sum();
//
//        return new TutorSalesResponse(tutorId, year, monthlySales, totalSales);
//    }

    @Transactional(readOnly = true)
    public TutorSalesResponse getSalesData(Long tutorId, int year) {

        List<TutorPayment> pastSettlements = tutorPaymentRepository.findByUserId(tutorId)
                .orElse(Collections.emptyList());

        YearMonth currentMonth = YearMonth.now();
        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate currentMonthEnd = currentMonth.atEndOfMonth();

        List<Payment> currentMonthPayments = paymentRepository.findByUserIdAndPaymentDateTimeBetween(tutorId, currentMonthStart.atStartOfDay(), currentMonthEnd.atTime(23, 59, 59));

        int currentMonthSales = currentMonthPayments.stream().mapToInt(Payment::getTotalAmount).sum();

        List<MonthlySales> monthlySales = pastSettlements.stream()
                .map(settlement -> new MonthlySales(settlement.getPeriodStartDate().getMonthValue(), settlement.getAmount()))
                .collect(Collectors.toList());

        monthlySales.add(new MonthlySales(currentMonth.getMonthValue(), currentMonthSales));

        int totalSales = monthlySales.stream().mapToInt(MonthlySales::getAmount).sum();

        return new TutorSalesResponse(tutorId, year, monthlySales, totalSales);
    }
}
