package com.liverpool.backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.security.service.JwtService;
import com.liverpool.backend.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    // Rutas sin autentificación
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth",
            "/api/swagger-ui",
            "/api/api-docs",
            "/api/v3/api-docs",
            "/api/swagger-resources",
            "/api/webjars");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String token = extractTokenFromRequest(request);

        // Si no hay token, continúa — Spring Security decidirá si el endpoint requiere
        // auth
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token presente pero inválido → responder 401 inmediatamente
        if (!jwtService.validateToken(token)) {
            sendUnauthorizedResponse(response, "Token JWT inválido o expirado");
            return;
        }

        try {
            // El token ya fue validado arriba; extraer claims una sola vez
            String email = jwtService.extractUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    sendUnauthorizedResponse(response, "Token no corresponde al usuario");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error en el token: {}", e.getMessage());
            sendUnauthorizedResponse(response, "No fue posible autenticar la solicitud");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = objectMapper.writeValueAsString(ApiResponse.error(message));
        response.getWriter().write(body);
    }
}
