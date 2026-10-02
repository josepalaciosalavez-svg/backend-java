package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Pedido;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PedidoResponse {

    private String id;
    private String codigoProducto;
    private Integer cantidad;
    private BigDecimal precio;
    private BigDecimal total;
    private Pedido.StatusPedido statusPedido;

    // Referencia al ID de entrega (dirección)
    private String entregaId;

    // Datos resueltos de la entrega y el cliente
    private String clienteId;
    private String nombreCliente;
    private String direccionEnvio;
    private String statusEntrega;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PedidoResponse from(Pedido pedido) {
        return from(pedido, null, null, null, null);
    }

    public static PedidoResponse from(Pedido pedido, String direccionEnvio, String statusEntrega, String clienteId, Cliente cliente) {
        BigDecimal total = pedido.getPrecio() != null && pedido.getCantidad() != null
                ? pedido.getPrecio().multiply(BigDecimal.valueOf(pedido.getCantidad()))
                : BigDecimal.ZERO;

        String nombreCompleto = null;
        if (cliente != null) {
            nombreCompleto = ((cliente.getNombre() != null ? cliente.getNombre() : "")
                    + (cliente.getApellidoPaterno() != null ? " " + cliente.getApellidoPaterno() : "")
                    + (cliente.getApellidoMaterno() != null ? " " + cliente.getApellidoMaterno() : "")).trim();
            if (clienteId == null) {
                clienteId = cliente.getId();
            }
        }

        return PedidoResponse.builder()
                .id(pedido.getId())
                .codigoProducto(pedido.getCodigoProducto())
                .cantidad(pedido.getCantidad())
                .precio(pedido.getPrecio())
                .total(total)
                .statusPedido(pedido.getStatusPedido())
                .entregaId(pedido.getEntregaId())
                .clienteId(clienteId)
                .nombreCliente(nombreCompleto)
                .direccionEnvio(direccionEnvio)
                .statusEntrega(statusEntrega)
                .createdAt(pedido.getCreatedAt())
                .updatedAt(pedido.getUpdatedAt())
                .build();
    }
}
