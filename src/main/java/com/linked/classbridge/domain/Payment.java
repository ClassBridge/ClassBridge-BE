package com.linked.classbridge.domain;

import com.linked.classbridge.dto.payment.KakaoStatusType;
import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String cid;

    private String partnerOrderId;

    private String partnerUserId;

    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int totalAmount;

    private String paymentMethodType;

    @Column(nullable = false, length = 20)
    private String tid;

    @Enumerated(EnumType.STRING)
    private PaymentStatusType status;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "payment")
    private Reservation reservation;

    public static Payment convertToPaymentEntity(PaymentApproveDto.Response response) {
        return Payment.builder()
                .cid(response.getCid())
                .tid(response.getTid())
                .partnerOrderId(response.getPartner_order_id())
                .partnerUserId(response.getPartner_user_id())
                .quantity(response.getQuantity())
                .totalAmount(response.getAmount().getTotal())
                .paymentMethodType(response.getPayment_method_type())
                .itemName(response.getItem_name())
                .status(PaymentStatusType.COMPLETED)
                .build();
    }

    // 수량 업데이트 메서드 추가
    public void calculateQuantity(int refundQuantity) {
        if (refundQuantity > this.quantity) {
            throw new RestApiException(ErrorCode.INVALID_REFUND_QUANTITY);
        }
        this.quantity -= refundQuantity;
    }
}
