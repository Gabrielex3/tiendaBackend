package com.exe.paymentservice.Dto;

import java.math.BigDecimal;
import java.util.List;

public record PagoRequest(
        Long usuarioId,
        String sessionId,
        String metodoPago,
        String observacion,
        String buyerName,
        String buyerEmail,
        String deliveryName,
        String deliveryRut,
        String deliveryPhone,
        String deliveryAddress,
        String deliveryNotes,
        List<ItemRequest> items
) {
    public record ItemRequest(Long productoId, String nombre, String imagenUrl, String talla, Integer cantidad, BigDecimal precioUnitario) {}
}
