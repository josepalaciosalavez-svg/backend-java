package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.DatosEntrega;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class DatosEntregaRequest {

    @NotBlank(message = "El cliente_id es requerido")
    private String clienteId;

    @NotBlank(message = "La dirección de envío es requerida")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String direccionEnvio;

    private DatosEntrega.StatusEntrega status;
}
