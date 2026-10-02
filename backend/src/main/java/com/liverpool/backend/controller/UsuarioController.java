package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.UsuarioRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.UsuarioResponse;
import com.liverpool.backend.model.Usuario;
import com.liverpool.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Administración de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<ApiResponse<PagedResponse<UsuarioResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Usuario.StatusUsuario status) {

        PagedResponse<UsuarioResponse> response = usuarioService.findAll(page, size, search, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario por ID")
    public ResponseEntity<ApiResponse<UsuarioResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear usuario (admin)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Usuario creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado exitosamente", null));
    }
}
