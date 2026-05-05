package com.smartpark.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private Long paymentId;
    private String paymentRef;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String gateway;
}
