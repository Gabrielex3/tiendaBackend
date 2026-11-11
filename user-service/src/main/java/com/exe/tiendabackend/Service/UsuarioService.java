package com.exe.tiendabackend.Service;

import com.exe.tiendabackend.Dto.ActualizarUsuarioRequest;
import com.exe.tiendabackend.Model.Role;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<Usuario> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional
    public Usuario actualizarPerfil(String username, ActualizarUsuarioRequest request) {
        Usuario usuario = obtenerPorUsername(username);
        if (request.phone() != null) usuario.setPhone(request.phone());
        if (request.address() != null) usuario.setAddress(request.address());
        if (request.comuna() != null) usuario.setComuna(request.comuna());
        // RUT solo se puede agregar una vez (no se puede cambiar después)
        if (request.rut() != null && !request.rut().isBlank()
                && (usuario.getRut() == null || usuario.getRut().isBlank())) {
            usuario.setRut(request.rut());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.imagenUrl() != null) usuario.setImagenUrl(request.imagenUrl());
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo actualizar el usuario", exception);
        }
    }

    @Transactional
    public Usuario actualizar(Long id, ActualizarUsuarioRequest request) {
        return aplicarCambios(obtenerPorId(id), request);
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(obtenerPorId(id));
    }

    private Usuario aplicarCambios(Usuario usuario, ActualizarUsuarioRequest request) {
        if (request.email() != null && !request.email().isBlank()) {
            if (usuarioRepository.existsByEmailAndIdNot(request.email(), usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
            }
            usuario.setEmail(request.email());
        }
        if (request.phone() != null) usuario.setPhone(request.phone());
        if (request.address() != null) usuario.setAddress(request.address());
        if (request.comuna() != null) usuario.setComuna(request.comuna());
        if (request.rut() != null) usuario.setRut(request.rut());
        if (request.role() != null && !request.role().isBlank()) {
            usuario.setRole(Role.valueOf(request.role()));
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.imagenUrl() != null) {
            usuario.setImagenUrl(request.imagenUrl());
        }
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo actualizar el usuario", exception);
        }
    }
}
