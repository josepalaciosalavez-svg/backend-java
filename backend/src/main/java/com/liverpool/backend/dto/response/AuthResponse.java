package com.liverpool.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UsuarioResponse usuario;

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UsuarioResponse usuario) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .usuario(usuario)
                .build();
    }
}
