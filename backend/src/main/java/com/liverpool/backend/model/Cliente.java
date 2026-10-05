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
@Document(collection = "clientes")
public class Cliente {

    @Id
    private String id;

    @Field("nombre")
    private String nombre;

    @Field("apellido_paterno")
    private String apellidoPaterno;

    @Field("apellido_materno")
    private String apellidoMaterno;

    @Indexed(unique = true)
    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("roles")
    private java.util.Set<Usuario.Rol> roles;

    @Field("status")
    @Builder.Default
    private StatusCliente status = StatusCliente.ACTIVO;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    public enum StatusCliente {
        ACTIVO, INACTIVO
    }
}
