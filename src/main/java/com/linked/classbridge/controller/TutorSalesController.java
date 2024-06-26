package com.linked.classbridge.controller;

import com.linked.classbridge.dto.sales.TutorSalesResponse;
import com.linked.classbridge.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tutors")
public class TutorSalesController {

    private final SalesService salesService;

    @Operation(summary = "강사 매출 조회", description = "강사 매출 내역을 조회합니다.")
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/sales")
    public TutorSalesResponse getTutorSales(@RequestParam int year) {
        return salesService.getSalesData(year);
    }
}
