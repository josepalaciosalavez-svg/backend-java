package com.liverpool.backend.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}
