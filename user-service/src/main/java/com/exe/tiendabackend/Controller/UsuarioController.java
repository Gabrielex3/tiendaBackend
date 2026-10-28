package com.exe.tiendabackend.Controller;

import com.exe.tiendabackend.Dto.ActualizarUsuarioRequest;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public Usuario perfil(@AuthenticationPrincipal UserDetails principal) {
        return usuarioService.obtenerPorUsername(principal.getUsername());
    }

    @PutMapping("/me")
    public Usuario actualizarPerfil(@AuthenticationPrincipal UserDetails principal,
                                    @Valid @RequestBody ActualizarUsuarioRequest request) {
        return usuarioService.actualizarPerfil(principal.getUsername(), request);
    }

}
