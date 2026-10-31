package com.exe.tiendabackend.Service;

import com.exe.tiendabackend.Dto.LoginRequest;
import com.exe.tiendabackend.Dto.RegisterRequest;
import com.exe.tiendabackend.Model.Role;
import com.exe.tiendabackend.Model.Usuario;
import com.exe.tiendabackend.Repository.UsuarioRepository;
import com.exe.tiendabackend.Setting.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final AuthenticationManager manager = mock(AuthenticationManager.class);
    private final JwtService jwt = mock(JwtService.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, encoder, manager, jwt);
    }

    @Test
    void registrationHashesPasswordAndAssignsUserRole() {
        when(jwt.generateToken(any())).thenReturn("signed-token");
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            Usuario saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        var response = service.register(new RegisterRequest(
                "ana", "password123", "ana@example.com", null, null, null, null, null));
        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).saveAndFlush(captor.capture());
        assertTrue(encoder.matches("password123", captor.getValue().getPassword()));
        assertEquals(Role.USER, captor.getValue().getRole());
        assertEquals("signed-token", response.token());
        assertEquals(1L, response.usuario().getId());
        assertEquals("ana", response.usuario().getUsername());
    }

    @Test
    void duplicateRegistrationReturnsConflictWithoutSaving() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(true);
        var exception = assertThrows(ResponseStatusException.class, () -> service.register(
                new RegisterRequest("ana", "password123", "ana@example.com", null, null, null, null, null)));
        assertEquals(409, exception.getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(jwt);
    }

    @Test
    void invalidCredentialsReturnUnauthorizedWithoutIssuingToken() {
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("ana", "incorrect")));
        assertEquals(401, exception.getStatusCode().value());
        verifyNoInteractions(jwt);
    }

    @Test
    void loginIssuesTokenForAuthenticatedUser() {
        var user = Usuario.builder().id(1L).username("ana").build();
        when(manager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwt.generateToken(user)).thenReturn("signed-token");
        var response = service.login(new LoginRequest("ana", "password123"));
        assertEquals("signed-token", response.token());
        assertEquals(1L, response.usuario().getId());
        assertEquals("ana", response.usuario().getUsername());
    }
}
