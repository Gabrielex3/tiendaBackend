package com.exe.tiendabackend.Setting;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @Test
    void malformedTokenReturnsUnauthorizedAndStopsChain() throws Exception {
        JwtService jwt = mock(JwtService.class);
        var users = mock(UserDetailsService.class);
        var chain = mock(FilterChain.class);
        when(jwt.extractUsername("invalid")).thenThrow(new MalformedJwtException("Invalid"));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        var response = new MockHttpServletResponse();
        new JwtAuthenticationFilter(jwt, users).doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        verifyNoInteractions(users, chain);
    }
}
