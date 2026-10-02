package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.EntregaRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.EntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.Entrega;
import com.liverpool.backend.service.EntregaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/entregas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Entregas", description = "Gestión de direcciones de entrega asociadas a clientes")
public class EntregaController {

    private final EntregaService entregaService;

    @GetMapping
    @Operation(summary = "Listar entregas")
    public ResponseEntity<ApiResponse<PagedResponse<EntregaResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Búsqueda por dirección o cliente_id") @RequestParam(required = false) String search,
            @RequestParam(required = false) Entrega.StatusEntrega status) {

        PagedResponse<EntregaResponse> response =
                entregaService.findAll(page, size, sortBy, sortDir, search, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener entrega por ID")
    public ResponseEntity<ApiResponse<EntregaResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(entregaService.findById(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Consultar entregas por cliente",
        description = "Retorna todas las direcciones de envío asociadas a un cliente específico"
    )
    public ResponseEntity<ApiResponse<PagedResponse<EntregaResponse>>> findByCliente(
            @PathVariable String clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<EntregaResponse> response = entregaService.findByClienteId(clienteId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @Operation(summary = "Crear entrega (dirección de envío)")
    public ResponseEntity<ApiResponse<EntregaResponse>> create(@Valid @RequestBody EntregaRequest request) {
        EntregaResponse response = entregaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Entrega creada exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar entrega")
    public ResponseEntity<ApiResponse<EntregaResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody EntregaRequest request) {
        EntregaResponse response = entregaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Entrega actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar entrega")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        entregaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Entrega eliminada exitosamente", null));
    }
}
