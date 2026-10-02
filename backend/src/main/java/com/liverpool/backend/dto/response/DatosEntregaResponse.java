package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.DatosEntrega;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DatosEntregaResponse {

    private String id;
    private String clienteId;
    private String direccionEnvio;
    private DatosEntrega.StatusEntrega status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DatosEntregaResponse from(DatosEntrega datosEntrega) {
        return DatosEntregaResponse.builder()
                .id(datosEntrega.getId())
                .clienteId(datosEntrega.getClienteId())
                .direccionEnvio(datosEntrega.getDireccionEnvio())
                .status(datosEntrega.getStatus())
                .createdAt(datosEntrega.getCreatedAt())
                .updatedAt(datosEntrega.getUpdatedAt())
                .build();
    }
}
