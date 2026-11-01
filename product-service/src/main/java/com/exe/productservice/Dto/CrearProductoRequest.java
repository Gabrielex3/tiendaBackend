package com.exe.productservice.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearProductoRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precio,
        Integer stock,
        String imagenUrl,
        String categoria,
        Boolean destacado,
        Boolean enCarrusel
) {}
