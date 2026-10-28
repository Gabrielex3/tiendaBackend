package com.exe.tiendabackend.Service;

import com.exe.tiendabackend.Dto.AuthResponse;
import com.exe.tiendabackend.Dto.LoginRequest;
import com.exe.tiendabackend.Dto.RegisterRequest;
import com.exe.tiendabackend.Model.Role;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Repository.UsuarioRepository;
import com.exe.tiendabackend.Setting.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera los 72 bytes");
        }
        if (usuarioRepository.existsByUsername(request.username())
                || usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario o correo ya está registrado");
        }
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .comuna(request.comuna())
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(usuario);
        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo registrar el usuario", exception);
        }
        return new AuthResponse(token, usuario);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            String identifier = request.username();
            if (identifier != null && identifier.contains("@")) {
                Usuario usuarioPorEmail = usuarioRepository.findByEmail(identifier).orElse(null);
                if (usuarioPorEmail != null) {
                    identifier = usuarioPorEmail.getUsername();
                }
            }
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.password()));
            Usuario usuario = (Usuario) authentication.getPrincipal();
            return new AuthResponse(jwtService.generateToken(usuario), usuario);
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", exception);
        }
    }
}
