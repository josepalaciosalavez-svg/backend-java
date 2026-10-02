package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.Usuario;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100)
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private Set<Usuario.Rol> roles;

    private Usuario.StatusUsuario status;
}
