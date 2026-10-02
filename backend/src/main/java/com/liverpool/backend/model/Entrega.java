package com.liverpool.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "entregas")
public class Entrega {

    @Id
    private String id;

    @Field("codigo_producto")
    private String codigoProducto;

    @Field("cantidad")
    private Integer cantidad;

    @Field("precio")
    private BigDecimal precio;

    @Field("status_pedido")
    @Builder.Default
    private StatusPedido statusPedido = StatusPedido.PENDIENTE;

    @Field("datos_entrega_id")
    private String datosEntregaId;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public enum StatusPedido {
        PENDIENTE, PROCESANDO, ENVIADO, ENTREGADO, CANCELADO
    }
}
