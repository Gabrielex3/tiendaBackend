package com.exe.tiendabackend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 255) String phone,
        @Size(max = 255) String address,
        @Size(max = 255) String comuna,
        @Size(max = 255) String rut,
        @Size(max = 500) String imagenUrl
) {}
