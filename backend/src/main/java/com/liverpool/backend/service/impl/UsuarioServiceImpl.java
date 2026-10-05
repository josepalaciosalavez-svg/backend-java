package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.LoginRequest;
import com.liverpool.backend.dto.request.UsuarioRequest;
import com.liverpool.backend.dto.response.AuthResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.UsuarioResponse;
import com.liverpool.backend.exception.DuplicateResourceException;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Usuario;
import com.liverpool.backend.repository.ClienteRepository;
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
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // 1. Verificar si es un usuario del sistema (ADMIN, USER, VIEWER)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            log.info("Login exitoso para usuario del sistema: {}", email);
            return AuthResponse.of(accessToken, refreshToken, jwtService.getJwtExpirationMs(), UsuarioResponse.from(usuario));
        }

        // 2. Verificar si es un cliente que ingresa al sistema
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        String nombreCompleto = ((cliente.getNombre() != null ? cliente.getNombre() : "")
                + (cliente.getApellidoPaterno() != null ? " " + cliente.getApellidoPaterno() : "")
                + (cliente.getApellidoMaterno() != null ? " " + cliente.getApellidoMaterno() : "")).trim();

        Set<Usuario.Rol> roles = cliente.getRoles() != null && !cliente.getRoles().isEmpty()
                ? cliente.getRoles()
                : Collections.singleton(Usuario.Rol.ROLE_CLIENTE);

        UsuarioResponse clienteResponse = UsuarioResponse.builder()
                .id(cliente.getId())
                .nombre(nombreCompleto)
                .email(cliente.getEmail())
                .roles(roles)
                .status(cliente.getStatus() == Cliente.StatusCliente.ACTIVO
                        ? Usuario.StatusUsuario.ACTIVO
                        : Usuario.StatusUsuario.INACTIVO)
                .clienteId(cliente.getId())
                .createdAt(cliente.getCreatedAt())
                .build();

        log.info("Login exitoso para cliente: {} con ID: {}", email, cliente.getId());
        return AuthResponse.of(accessToken, refreshToken, jwtService.getJwtExpirationMs(), clienteResponse);
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
                .clienteId(request.getClienteId())
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
        if (request.getClienteId() != null) {
            usuario.setClienteId(request.getClienteId());
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
