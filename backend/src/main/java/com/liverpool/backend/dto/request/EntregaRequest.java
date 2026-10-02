package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.Entrega;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class EntregaRequest {

    @NotBlank(message = "El código de producto es requerido")
    private String codigoProducto;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private Entrega.StatusPedido statusPedido;

    private String datosEntregaId;
}
