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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

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
        return aplicarCambios(obtenerPorUsername(username), request);
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
        if (request.role() != null && !request.role().isBlank()) {
            usuario.setRole(Role.valueOf(request.role()));
        }
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo actualizar el usuario", exception);
        }
    }
}
