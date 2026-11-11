package com.exe.tiendabackend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @Email @Size(max = 255) String email,
        @Size(max = 255) String phone,
        @Size(max = 255) String address,
        @Size(max = 255) String comuna,
        @Size(max = 255) String rut,
        @Size(max = 20) String role,
        @Size(min = 8, max = 72) String password,
        @Size(max = 512) String imagenUrl
) {}
