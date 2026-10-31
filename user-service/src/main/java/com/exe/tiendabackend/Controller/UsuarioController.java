package com.exe.tiendabackend.Controller;

import com.exe.tiendabackend.Dto.ActualizarUsuarioRequest;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        // Solo el propio usuario (o admin) puede ver la foto
        if (principal != null) {
            try {
                Usuario authUser = usuarioService.obtenerPorUsername(principal.getUsername());
                boolean isAdmin = authUser.getRole() == com.exe.tiendabackend.Model.Role.ADMIN;
                if (!isAdmin && !authUser.getId().equals(id)) {
                    return ResponseEntity.status(403).build();
                }
            } catch (Exception ignored) {}
        } else {
            return ResponseEntity.status(401).build();
        }
        Usuario usuario = usuarioService.obtenerPorId(id);
        String url = usuario.getImagenUrl();
        if (url == null || url.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(resp.statusCode()).build();
            }
            String contentType = resp.headers().firstValue("Content-Type").orElse("image/jpeg");
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType).body(resp.body());
        } catch (Exception e) {
            return ResponseEntity.status(502).build();
        }
    }
}
