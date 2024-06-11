package com.linked.classbridge.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RefundPolicy {
    public static double calculateRefundRate(LocalDate lessonDate, LocalTime lessonStartTime, LocalDateTime refundRequestDate) {
        LocalDateTime lessDateTime = LocalDateTime.of(lessonDate, lessonStartTime);
        long daysBetween = Duration.between(refundRequestDate, lessDateTime).toDays();

        if (daysBetween >= 4) {
            return 1.0; // 100% 환불
        } else if (daysBetween == 3) {
            return 0.7; // 70% 환불
        } else if (daysBetween == 2) {
            return 0.5; // 50% 환불
        } else {
            return 0.0; // 환불 없음
        }
    }

    public static void main(String[] args) {
        Integer integer = null;
        System.out.println(integer);
    }

}
