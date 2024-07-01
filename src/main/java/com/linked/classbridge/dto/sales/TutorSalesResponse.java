package com.linked.classbridge.dto.sales;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TutorSalesResponse {
    private Long tutorId;
    private int year;
    private List<MonthlySales> monthlySales;
    private int totalSales;
//    private List<ClassSales> classSales;
    private List<ClassMonthlySales> classMonthlySales;

}
