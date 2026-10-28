package com.exe.tiendabackend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @Email @Size(max = 255) String email,
        @Size(max = 255) String phone,
        @Size(max = 255) String address,
        @Size(max = 255) String comuna,
        @Size(max = 20) String role
) {}
