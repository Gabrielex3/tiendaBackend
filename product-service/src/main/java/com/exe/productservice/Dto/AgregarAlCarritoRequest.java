package com.exe.productservice.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgregarAlCarritoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        String talla
) {}
