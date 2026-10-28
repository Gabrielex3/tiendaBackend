package com.exe.tiendabackend.Service;

import com.exe.tiendabackend.Dto.ActualizarUsuarioRequest;
import com.exe.tiendabackend.Model.Role;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final UsuarioService service = new UsuarioService(repository);

    @Test
    void profileUpdatePreservesCredentialsAndRole() {
        var usuario = Usuario.builder().id(1L).username("ana")
                .password("hashed-password").role(Role.USER).email("old@example.com").build();
        when(repository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(repository.saveAndFlush(usuario)).thenReturn(usuario);
        var updated = service.actualizarPerfil("ana",
                new ActualizarUsuarioRequest("new@example.com", "123", "Calle 1", "Santiago"));
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("123", updated.getPhone());
        assertEquals("Calle 1", updated.getAddress());
        assertEquals("Santiago", updated.getComuna());
        assertEquals("ana", updated.getUsername());
        assertEquals("hashed-password", updated.getPassword());
        assertEquals(Role.USER, updated.getRole());
    }

    @Test
    void duplicateEmailDoesNotModifyProfile() {
        var usuario = Usuario.builder().id(1L).email("old@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);
        var exception = assertThrows(ResponseStatusException.class, () -> service.actualizar(1L,
                new ActualizarUsuarioRequest("taken@example.com", null, null, null)));
        assertEquals(409, exception.getStatusCode().value());
        assertEquals("old@example.com", usuario.getEmail());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deletingMissingUserReturnsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class, () -> service.eliminar(99L));
        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).delete(any(Usuario.class));
    }
}
