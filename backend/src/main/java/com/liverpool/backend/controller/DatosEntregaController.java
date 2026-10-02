package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.DatosEntregaRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.DatosEntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.DatosEntrega;
import com.liverpool.backend.service.DatosEntregaService;
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
@RequestMapping("/datos-entrega")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Datos de Entrega", description = "Gestión de datos de entrega asociados a clientes")
public class DatosEntregaController {

    private final DatosEntregaService datosEntregaService;

    @GetMapping
    @Operation(summary = "Listar datos de entrega")
    public ResponseEntity<ApiResponse<PagedResponse<DatosEntregaResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Búsqueda por dirección o cliente_id") @RequestParam(required = false) String search,
            @RequestParam(required = false) DatosEntrega.StatusEntrega status) {

        PagedResponse<DatosEntregaResponse> response =
                datosEntregaService.findAll(page, size, sortBy, sortDir, search, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener datos de entrega por ID")
    public ResponseEntity<ApiResponse<DatosEntregaResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(datosEntregaService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear datos de entrega")
    public ResponseEntity<ApiResponse<DatosEntregaResponse>> create(@Valid @RequestBody DatosEntregaRequest request) {
        DatosEntregaResponse response = datosEntregaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Datos de entrega creados exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de entrega")
    public ResponseEntity<ApiResponse<DatosEntregaResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody DatosEntregaRequest request) {
        DatosEntregaResponse response = datosEntregaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Datos de entrega actualizados exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar datos de entrega")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        datosEntregaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Datos de entrega eliminados exitosamente", null));
    }
}
