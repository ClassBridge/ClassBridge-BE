package com.linked.classbridge.dto.payment;

import lombok.Getter;
import lombok.Setter;

public class PaymentApproveDto {
    @Getter
    @Setter
    public static class Response {
        private String tid; // 결제 고유 번호
        private String cid; // 가맹점 코드
        private String sid; // 정기결제용 ID
        private String partnerOrderId; // 가맹점 주문 번호
        private String partnerUserId; // 가맹점 회원 id
        private String paymentMethodType; // 결제 수단
        private Amount amount; // 결제 금액 정보
        private String itemName; // 상품명
        private String itemCode; // 상품 코드
        private Long quantity; // 상품 수량
        private String approved_at; // 결제 승인 시간
        private String payload; // 결제 승인 요청에 대해 저장 값, 요청 시 전달 내용
    }
}
