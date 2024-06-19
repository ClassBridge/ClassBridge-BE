package com.linked.classbridge.dto.sales;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySales {
    private int month;
    private int amount;

    public MonthlySales(int month, int amount) {
        this.month = month;
        this.amount = amount;
    }
}
