package com.linked.classbridge.dto.sales;

import com.linked.classbridge.dto.sales.MonthlySales;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorSalesResponse {
    private Long tutorId;
    private int year;
    private List<MonthlySales> monthlySales;
    private int totalSales;

    public TutorSalesResponse(Long tutorId, int year, List<MonthlySales> monthlySales, int totalSales) {
        this.tutorId = tutorId;
        this.year = year;
        this.monthlySales = monthlySales;
        this.totalSales = totalSales;
    }
}
