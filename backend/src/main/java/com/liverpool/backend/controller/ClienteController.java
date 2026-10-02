package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.ClienteRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.ClienteResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Gestión de módulo de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene la lista paginada de clientes con soporte de búsqueda")
    public ResponseEntity<ApiResponse<PagedResponse<ClienteResponse>>> findAll(
            @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Registros por página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección: asc o desc") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Término de búsqueda en nombre, apellidos o email") @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) Cliente.StatusCliente status) {

        PagedResponse<ClienteResponse> response = clienteService.findAll(page, size, sortBy, sortDir, search, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente por ID")
    public ResponseEntity<ApiResponse<ClienteResponse>> findById(@PathVariable String id) {
        ClienteResponse response = clienteService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Crear cliente")
    public ResponseEntity<ApiResponse<ClienteResponse>> create(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Cliente creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cliente")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        clienteService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente eliminado exitosamente", null));
    }
}
