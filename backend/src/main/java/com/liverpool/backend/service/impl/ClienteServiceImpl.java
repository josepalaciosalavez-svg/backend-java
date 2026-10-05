package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.ClienteRequest;
import com.liverpool.backend.dto.response.ClienteResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.exception.DuplicateResourceException;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.repository.ClienteRepository;
import com.liverpool.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.liverpool.backend.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PagedResponse<ClienteResponse> findAll(int page, int size, String sortBy,
                                                   String sortDir, String search,
                                                   Cliente.StatusCliente status) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Cliente> clientes;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = status != null;

        if (hasSearch && hasStatus) {
            clientes = clienteRepository.searchByTermAndStatus(search, status, pageable);
        } else if (hasSearch) {
            clientes = clienteRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            clientes = clienteRepository.findByStatus(status, pageable);
        } else {
            clientes = clienteRepository.findAll(pageable);
        }

        return PagedResponse.of(clientes.map(ClienteResponse::from));
    }

    @Override
    public ClienteResponse findById(String id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return ClienteResponse.from(cliente);
    }

    @Override
    public ClienteResponse create(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cliente", "email", request.getEmail());
        }

        String rawPassword = StringUtils.hasText(request.getPassword())
                ? request.getPassword()
                : "Cliente@2024!";

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(rawPassword))
                .roles(Collections.singleton(Usuario.Rol.ROLE_CLIENTE))
                .status(request.getStatus() != null ? request.getStatus() : Cliente.StatusCliente.ACTIVO)
                .build();

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado con id: {}", saved.getId());
        return ClienteResponse.from(saved);
    }

    @Override
    public ClienteResponse update(String id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Verificar email duplicado si se está cambiando
        if (!cliente.getEmail().equals(request.getEmail())
                && clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cliente", "email", request.getEmail());
        }

        cliente.setNombre(request.getNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setEmail(request.getEmail().toLowerCase().trim());
        if (StringUtils.hasText(request.getPassword())) {
            cliente.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            cliente.setStatus(request.getStatus());
        }

        Cliente updated = clienteRepository.save(cliente);
        log.info("Cliente actualizado con id: {}", updated.getId());
        return ClienteResponse.from(updated);
    }

    @Override
    public void delete(String id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", "id", id);
        }
        clienteRepository.deleteById(id);
        log.info("Cliente eliminado con id: {}", id);
    }
}
