package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.Cliente;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClienteResponse {

    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private Cliente.StatusCliente status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClienteResponse from(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellidoPaterno(cliente.getApellidoPaterno())
                .apellidoMaterno(cliente.getApellidoMaterno())
                .email(cliente.getEmail())
                .status(cliente.getStatus())
                .createdAt(cliente.getCreatedAt())
                .updatedAt(cliente.getUpdatedAt())
                .build();
    }
}
