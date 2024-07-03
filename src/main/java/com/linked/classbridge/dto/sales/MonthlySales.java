package com.linked.classbridge.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthlySales {
    private int month;
    private int amount;
}
