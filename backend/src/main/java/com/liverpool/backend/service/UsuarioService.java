package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.LoginRequest;
import com.liverpool.backend.dto.request.UsuarioRequest;
import com.liverpool.backend.dto.response.AuthResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.UsuarioResponse;
import com.liverpool.backend.model.Usuario;

public interface UsuarioService {

    AuthResponse login(LoginRequest request);

    UsuarioResponse register(UsuarioRequest request);

    PagedResponse<UsuarioResponse> findAll(int page, int size, String search, Usuario.StatusUsuario status);

    UsuarioResponse findById(String id);

    UsuarioResponse update(String id, UsuarioRequest request);

    void delete(String id);
}
