package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.Pedido;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    @NotBlank(message = "El código de producto es requerido")
    @Schema(description = "Código del producto", example = "LIV-ELEC-001")
    private String codigoProducto;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Schema(description = "Cantidad de productos", example = "2")
    private Integer cantidad;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio unitario", example = "4599.00")
    private BigDecimal precio;

    @Schema(description = "Estatus del pedido", example = "PENDIENTE")
    private Pedido.StatusPedido statusPedido;

    @NotBlank(message = "El ID de entrega es requerido")
    @Schema(description = "ID de la entrega asociada (dirección del cliente)", example = "651a2b3c4d5e6f7a8b9c0d1e")
    private String entregaId;
}
