package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Entrega;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EntregaResponse {

    private String id;
    private String clienteId;
    private String nombreCliente;
    private String direccionEnvio;
    private Entrega.StatusEntrega status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EntregaResponse from(Entrega entrega) {
        return from(entrega, null);
    }

    public static EntregaResponse from(Entrega entrega, Cliente cliente) {
        String nombreCompleto = null;
        if (cliente != null) {
            nombreCompleto = ((cliente.getNombre() != null ? cliente.getNombre() : "")
                    + (cliente.getApellidoPaterno() != null ? " " + cliente.getApellidoPaterno() : "")
                    + (cliente.getApellidoMaterno() != null ? " " + cliente.getApellidoMaterno() : "")).trim();
        }

        return EntregaResponse.builder()
                .id(entrega.getId())
                .clienteId(entrega.getClienteId())
                .nombreCliente(nombreCompleto)
                .direccionEnvio(entrega.getDireccionEnvio())
                .status(entrega.getStatus())
                .createdAt(entrega.getCreatedAt())
                .updatedAt(entrega.getUpdatedAt())
                .build();
    }
}
