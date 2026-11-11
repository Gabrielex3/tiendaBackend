package com.exe.productservice.Dto;

import jakarta.validation.constraints.DecimalMin;



import java.math.BigDecimal;

public record ActualizarProductoRequest(
        String nombre,
        String descripcion,
        @DecimalMin(value = "0.01") BigDecimal precio,
        Integer stock,
        String imagenUrl,
        String categoria,
        Boolean activo,
        Boolean destacado,
        Boolean enCarrusel
) {}
