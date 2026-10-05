package com.liverpool.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.security.filter.JwtAuthenticationFilter;
import com.liverpool.backend.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    /**
     * Orígenes permitidos para CORS. Configura mediante la variable de entorno
     * CORS_ALLOWED_ORIGINS.
     * En producción especifica el dominio real; en desarrollo acepta localhost.
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
    private List<String> allowedOrigins;

    private static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // Respuesta JSON cuando no hay token (en lugar de redirigir al login HTML de
                // Spring)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(PUBLIC_PATHS).permitAll()
                        .antMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .antMatchers(HttpMethod.POST, "/clientes/**").hasAnyRole("ADMIN", "USER")
                        .antMatchers(HttpMethod.PUT, "/clientes/**").hasAnyRole("ADMIN", "USER")
                        .antMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/entregas/**").hasAnyRole("ADMIN", "USER", "VIEWER", "CLIENTE")
                        .antMatchers(HttpMethod.POST, "/entregas/**").hasAnyRole("ADMIN", "USER", "CLIENTE")
                        .antMatchers(HttpMethod.PUT, "/entregas/**").hasAnyRole("ADMIN", "USER", "CLIENTE")
                        .antMatchers(HttpMethod.DELETE, "/entregas/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("ADMIN", "USER", "VIEWER", "CLIENTE")
                        .antMatchers(HttpMethod.POST, "/pedidos/**").hasAnyRole("ADMIN", "USER")
                        .antMatchers(HttpMethod.PUT, "/pedidos/**").hasAnyRole("ADMIN", "USER")
                        .antMatchers(HttpMethod.DELETE, "/pedidos/**").hasRole("ADMIN")
                        .antMatchers("/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Devuelve JSON {success:false, message:"..."} con 401 cuando el endpoint
     * requiere
     * autenticación y el request llega sin token (o sin autenticación válida en el
     * contexto).
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("Acceso no autenticado a {}: {}", request.getRequestURI(), authException.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String body = objectMapper.writeValueAsString(
                    ApiResponse.error("Autenticación requerida. Incluye un token Bearer válido."));
            response.getWriter().write(body);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
