package com.exe.tiendabackend.Dto;

import com.exe.tiendabackend.Model.Usuario;

public record AuthResponse(String token, Usuario usuario) {}
