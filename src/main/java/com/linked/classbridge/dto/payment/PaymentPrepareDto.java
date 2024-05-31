package com.linked.classbridge.dto.payment;

import lombok.Getter;
import lombok.Setter;

public class PaymentPrepareDto {
    @Getter
    @Setter
    public static class Request {
        private String partnerOrderId;      // 가맹점 주문 번호
        private String partnerUserId;       // 가맹점 회원 id
        private String itemName;            // 상품명
        private int quantity;               // 상품 수량
        private int totalAmount;            // 상품 총액
        private int texFreeAmount;          // 상품 비과세 금액
    }

    @Getter
    public static class Response {
        private String partnerOrderId;      // 가맹점 주문 번호
        private String partnerUserId;       // 가맹점 회원 id
        private String tid;                 // 결제 고유 번호
        private String next_redirect_pc_url;// pc 웹일 경우 받는 결제 페이지
        private String created_at;
        @Setter
        private String pgToken;
    }
}
