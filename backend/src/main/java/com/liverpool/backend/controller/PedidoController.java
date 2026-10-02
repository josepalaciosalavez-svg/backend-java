package com.liverpool.backend.controller;

import com.liverpool.backend.dto.request.PedidoRequest;
import com.liverpool.backend.dto.response.ApiResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.PedidoResponse;
import com.liverpool.backend.model.Pedido;
import com.liverpool.backend.service.PedidoService;
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
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos", description = "Gestión de pedidos de productos y consulta de entregas")
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    @Operation(
        summary = "Listar pedidos",
        description = "Retorna el listado paginado de pedidos con el nombre del cliente y dirección de entrega"
    )
    public ResponseEntity<ApiResponse<PagedResponse<PedidoResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Búsqueda por código de producto") @RequestParam(required = false) String search,
            @RequestParam(required = false) Pedido.StatusPedido statusPedido) {

        PagedResponse<PedidoResponse> response =
                pedidoService.findAll(page, size, sortBy, sortDir, search, statusPedido);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Revisar pedido por ID",
        description = "Muestra el detalle del producto, cantidad, precio, estatus, nombre del cliente y dirección de entrega"
    )
    public ResponseEntity<ApiResponse<PedidoResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.findById(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Consultar pedidos por cliente",
        description = "Retorna todos los pedidos asociados a un cliente específico a través de sus entregas"
    )
    public ResponseEntity<ApiResponse<PagedResponse<PedidoResponse>>> findByCliente(
            @PathVariable String clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<PedidoResponse> response = pedidoService.findByClienteId(clienteId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/entrega/{entregaId}")
    @Operation(
        summary = "Consultar pedidos por entrega",
        description = "Retorna todos los pedidos asociados a una entrega (dirección) específica"
    )
    public ResponseEntity<ApiResponse<PagedResponse<PedidoResponse>>> findByEntrega(
            @PathVariable String entregaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<PedidoResponse> response = pedidoService.findByEntregaId(entregaId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @Operation(summary = "Crear pedido")
    public ResponseEntity<ApiResponse<PedidoResponse>> create(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = pedidoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Pedido creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido")
    public ResponseEntity<ApiResponse<PedidoResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = pedidoService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Pedido actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        pedidoService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Pedido eliminado exitosamente", null));
    }
}
