package com.liverpool.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "datos_entrega")
public class DatosEntrega {

    @Id
    private String id;

    @Indexed
    @Field("cliente_id")
    private String clienteId;

    @Field("direccion_envio")
    private String direccionEnvio;

    @Field("status")
    @Builder.Default
    private StatusEntrega status = StatusEntrega.PENDIENTE;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public enum StatusEntrega {
        PENDIENTE, EN_PROCESO, ENTREGADO, CANCELADO
    }
}
