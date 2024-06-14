package com.linked.classbridge.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 결제 시 주문 번호 생성
 */
public class OrderNumberGenerator {
    public static String generateOrderNumber(String email) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "ORD-" + email + "-" + timestamp + "-" + uuid;
    }
}
