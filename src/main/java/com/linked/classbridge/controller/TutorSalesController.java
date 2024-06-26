package com.linked.classbridge.controller;

import com.linked.classbridge.dto.sales.TutorSalesResponse;
import com.linked.classbridge.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tutors")
public class TutorSalesController {

    private final SalesService salesService;

    @GetMapping("/sales")
    public TutorSalesResponse getTutorSales(@RequestParam int year) {
        return salesService.getSalesData(year);
    }
}
