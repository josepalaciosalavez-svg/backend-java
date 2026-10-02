package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.LoginRequest;
import com.liverpool.backend.dto.request.UsuarioRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.AuthResponse;
import com.liverpool.backend.dto.response.UsuarioResponse;
import com.liverpool.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "WS para login y registro de usuarios")
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica del usuario")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = usuarioService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crer usuario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Usuario registrado exitosamente", response));
    }
}
