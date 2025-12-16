package com.immopilot.modules.rental.service.dto;

import com.immopilot.modules.rental.domain.PaymentStatus;
import com.immopilot.modules.rental.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPaymentDTO {
    private Long id;
    private Long leaseId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String receiptUrl;
}
