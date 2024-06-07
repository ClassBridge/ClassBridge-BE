package com.linked.classbridge.domain;

import com.linked.classbridge.dto.payment.PaymentApproveDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    //        @Column(nullable = false, length = 10)
    private String cid;

    //        @Column(nullable = false, length = 100)
    private String partnerOrderId;

    //        @Column(nullable = false, length = 100)
    private String partnerUserId;

    //        @Column(nullable = false, length = 100)
    private String itemName;

    //        @Column(length = 100)
    private String itemCode;

    //        @Column(nullable = false)
    private Long quantity;

    //        @Column(nullable = false)
    private int totalAmount;

    //        @Column(length = 255)
    private String paymentMethodType;

    private Integer installMonth;

    //        @Column(nullable = false, length = 20)
    private String tid;

    //        @Column(nullable = false)
//    private LocalDateTime createdAt;

    //        @Column(nullable = false, length = 255, columnDefinition = "varchar(255) default 'PENDING'")
    private String status;

    public static Payment convertToPaymentEntity(PaymentApproveDto.Response response) {
        return Payment.builder()
                .cid(response.getCid())
                .tid(response.getTid())
                .partnerOrderId(response.getPartnerOrderId())
                .partnerUserId(response.getPartnerUserId())
                .quantity(response.getQuantity())
                .totalAmount(response.getAmount().getTotal())
                .paymentMethodType(response.getPaymentMethodType())
                .status("COMPLETED")
                .build();
    }
}
