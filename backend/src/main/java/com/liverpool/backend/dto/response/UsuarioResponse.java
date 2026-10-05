package com.liverpool.backend.dto.response;

import com.liverpool.backend.model.Usuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UsuarioResponse {

    private String id;
    private String nombre;
    private String email;
    private Set<Usuario.Rol> roles;
    private Usuario.StatusUsuario status;
    private String clienteId;
    private LocalDateTime createdAt;

    public static UsuarioResponse from(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .roles(usuario.getRoles())
                .status(usuario.getStatus())
                .clienteId(usuario.getClienteId())
                .createdAt(usuario.getCreatedAt())
                .build();
    }
}
