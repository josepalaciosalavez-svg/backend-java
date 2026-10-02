package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.Entrega;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaRequest {

    @NotBlank(message = "El clienteId es requerido")
    @Schema(description = "ID del cliente asociado a esta entrega", example = "651a111c4d5e6f7a8b9c0d11")
    private String clienteId;

    @NotBlank(message = "La dirección de envío es requerida")
    @Schema(description = "Dirección completa de entrega", example = "Av. Insurgentes Sur 123, Col. Del Valle, CDMX, CP 03100")
    private String direccionEnvio;

    @Schema(description = "Estatus de la entrega", example = "PENDIENTE")
    private Entrega.StatusEntrega status;
}
