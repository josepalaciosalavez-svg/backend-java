package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.DatosEntrega;
import com.liverpool.backend.model.Entrega;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EntregaResponse {

    private String id;
    private String codigoProducto;
    private Integer cantidad;
    private BigDecimal precio;
    private BigDecimal total;
    private Entrega.StatusPedido statusPedido;

    // Referencia a datos de entrega
    private String datosEntregaId;

    // Datos desnormalizados para no requerir una segunda llamada desde el cliente
    private String clienteId;
    private String direccionEnvio;
    private DatosEntrega.StatusEntrega statusEntrega;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Mapeo sin datos de entrega (usado en listados paginados donde
     * no se hace join para mantener el rendimiento).
     */
    public static EntregaResponse from(Entrega entrega) {
        return from(entrega, null);
    }

    /**
     * Mapeo completo con datos de entrega y cliente embebidos.
     * Usado en create, update y findById.
     */
    public static EntregaResponse from(Entrega entrega, DatosEntrega datosEntrega) {
        BigDecimal total = entrega.getPrecio() != null && entrega.getCantidad() != null
                ? entrega.getPrecio().multiply(BigDecimal.valueOf(entrega.getCantidad()))
                : BigDecimal.ZERO;

        EntregaResponseBuilder builder = EntregaResponse.builder()
                .id(entrega.getId())
                .codigoProducto(entrega.getCodigoProducto())
                .cantidad(entrega.getCantidad())
                .precio(entrega.getPrecio())
                .total(total)
                .statusPedido(entrega.getStatusPedido())
                .datosEntregaId(entrega.getDatosEntregaId())
                .createdAt(entrega.getCreatedAt())
                .updatedAt(entrega.getUpdatedAt());

        // Si se pasaron los datos de entrega, agrega cliente y dirección al response
        if (datosEntrega != null) {
            builder.clienteId(datosEntrega.getClienteId())
                   .direccionEnvio(datosEntrega.getDireccionEnvio())
                   .statusEntrega(datosEntrega.getStatus());
        }

        return builder.build();
    }
}
