package com.immopilot.modules.rental.service.dto;

import java.math.BigDecimal;

public record PropertyRentalStats(
        BigDecimal totalUnpaid,
        BigDecimal totalRevenueYear) {
}
