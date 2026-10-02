package com.liverpool.backend.dto.request;

import com.liverpool.backend.model.Cliente;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ClienteRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido paterno es requerido")
    @Size(min = 2, max = 60, message = "El apellido paterno debe tener entre 2 y 60 caracteres")
    private String apellidoPaterno;

    @Size(max = 60, message = "El apellido materno no puede exceder 60 caracteres")
    private String apellidoMaterno;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;

    private Cliente.StatusCliente status;
}
