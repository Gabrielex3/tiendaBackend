package com.exe.paymentservice.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(Long id, String receiptNumber, BigDecimal total, LocalDateTime createdAt) {}
