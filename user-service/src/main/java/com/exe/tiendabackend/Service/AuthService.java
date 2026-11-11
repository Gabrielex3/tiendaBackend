package com.exe.tiendabackend.Service;

import com.exe.tiendabackend.Dto.AuthResponse;
import com.exe.tiendabackend.Dto.LoginRequest;
import com.exe.tiendabackend.Dto.RegisterRequest;
import com.exe.tiendabackend.Model.Role;
import com.exe.tiendabackend.Model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private boolean rutValido(String rut) {
        if (rut == null || rut.isBlank()) return true;
        String limpio = rut.replace(".", "").replace("-", "").toUpperCase();
        if (!limpio.matches("^[0-9]+[0-9K]$")) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int mult = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * mult;
            mult = (mult == 7) ? 2 : mult + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalc = (resto == 11) ? '0' : (resto == 10) ? 'K' : Character.forDigit(resto, 10);
        return dv == dvCalc;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!rutValido(request.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
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
                .rut(request.rut())
                .imagenUrl(request.imagenUrl())
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

    public AuthResponse loginWithGoogle(String credential) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + credential))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(resp.body());
            String email = node.get("email").asText();
            String name = node.has("name") ? node.get("name").asText() : email;
            String googleId = node.get("sub").asText();
            String picture = node.has("picture") ? node.get("picture").asText() : null;

            String configuredClientId = System.getenv("GOOGLE_CLIENT_ID");
            if (configuredClientId != null && !configuredClientId.isBlank() && !configuredClientId.startsWith("TU_")) {
                String aud = node.has("aud") ? node.get("aud").asText() : null;
                if (aud == null || !aud.equals(configuredClientId)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client ID de Google no coincide");
                }
            }

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no registrado. Regístrate primero.");
            } else if (picture != null && (usuario.getImagenUrl() == null || usuario.getImagenUrl().isBlank())) {
                usuario.setImagenUrl(picture);
                usuario = usuarioRepository.save(usuario);
            }
            return new AuthResponse(jwtService.generateToken(usuario), usuario);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo autenticar con Google", e);
        }
    }

    public AuthResponse registerWithGoogle(String credential) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + credential))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(resp.body());
            String email = node.get("email").asText();
            String name = node.has("name") ? node.get("name").asText() : email;
            String googleId = node.get("sub").asText();
            String picture = node.has("picture") ? node.get("picture").asText() : null;

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                // Ya existe, solo login
                if (picture != null && (usuario.getImagenUrl() == null || usuario.getImagenUrl().isBlank())) {
                    usuario.setImagenUrl(picture);
                    usuario = usuarioRepository.save(usuario);
                }
                return new AuthResponse(jwtService.generateToken(usuario), usuario);
            }
            // Crear usuario con datos de Google (campos vacíos para completar)
            usuario = Usuario.builder()
                    .username((email.split("@")[0] + "_" + googleId.substring(0, Math.min(6, googleId.length()))))
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .email(email)
                    .imagenUrl(picture)
                    .role(Role.USER)
                    .build();
            usuario = usuarioRepository.save(usuario);
            return new AuthResponse(jwtService.generateToken(usuario), usuario);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo registrar con Google", e);
        }
    }
}
