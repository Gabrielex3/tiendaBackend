package com.exe.tiendabackend.Dto;

import com.exe.tiendabackend.Model.Usuario;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthResponseTest {
    @Test
    void serializedUserNeverIncludesPassword() {
        var usuario = Usuario.builder()
                .id(1L).username("ana").password("secret-hash")
                .email("ana@example.com").phone("123456789")
                .address("Dirección privada").comuna("Comuna privada")
                .build();
        var mapper = JsonMapper.builder().build();
        var json = mapper.readTree(mapper.writeValueAsString(
                new AuthResponse("signed-token", usuario)));
        assertFalse(json.get("usuario").has("password"));
        assertFalse(mapper.writeValueAsString(usuario).contains("secret-hash"));
        assertEquals("ana", json.get("usuario").get("username").asString());
        assertEquals("ana@example.com", json.get("usuario").get("email").asString());
        assertEquals("signed-token", json.get("token").asString());
    }
}
