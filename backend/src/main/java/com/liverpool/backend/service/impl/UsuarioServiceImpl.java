package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.LoginRequest;
import com.liverpool.backend.dto.request.UsuarioRequest;
import com.liverpool.backend.dto.response.AuthResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.UsuarioResponse;
import com.liverpool.backend.exception.DuplicateResourceException;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.Usuario;
import com.liverpool.backend.repository.UsuarioRepository;
import com.liverpool.backend.security.service.JwtService;
import com.liverpool.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", request.getEmail()));

        log.info("Login exitoso para: {}", request.getEmail());
        return AuthResponse.of(accessToken, refreshToken, jwtService.getJwtExpirationMs(), UsuarioResponse.from(usuario));
    }

    @Override
    public UsuarioResponse register(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getEmail());
        }

        Usuario.StatusUsuario status = request.getStatus() != null
                ? request.getStatus()
                : Usuario.StatusUsuario.ACTIVO;

        java.util.Set<Usuario.Rol> roles = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? request.getRoles()
                : new HashSet<>(Collections.singletonList(Usuario.Rol.ROLE_USER));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .status(status)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario registrado con id: {}", saved.getId());
        return UsuarioResponse.from(saved);
    }

    @Override
    public PagedResponse<UsuarioResponse> findAll(int page, int size, String search, Usuario.StatusUsuario status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Usuario> usuarios;

        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = status != null;

        if (hasSearch) {
            usuarios = usuarioRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            usuarios = usuarioRepository.findByStatus(status, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }

        return PagedResponse.of(usuarios.map(UsuarioResponse::from));
    }

    @Override
    public UsuarioResponse findById(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return UsuarioResponse.from(usuario);
    }

    @Override
    public UsuarioResponse update(String id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getEmail());
        }

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail().toLowerCase().trim());
        if (StringUtils.hasText(request.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            usuario.setRoles(request.getRoles());
        }
        if (request.getStatus() != null) {
            usuario.setStatus(request.getStatus());
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado con id: {}", updated.getId());
        return UsuarioResponse.from(updated);
    }

    @Override
    public void delete(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado con id: {}", id);
    }
}
