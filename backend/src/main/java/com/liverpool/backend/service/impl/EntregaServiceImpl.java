package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.EntregaRequest;
import com.liverpool.backend.dto.response.EntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Entrega;
import com.liverpool.backend.repository.ClienteRepository;
import com.liverpool.backend.repository.EntregaRepository;
import com.liverpool.backend.service.EntregaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public PagedResponse<EntregaResponse> findAll(int page, int size, String sortBy,
                                                  String sortDir, String search,
                                                  Entrega.StatusEntrega status) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Entrega> entregas;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = status != null;

        if (hasSearch && hasStatus) {
            entregas = entregaRepository.searchByTermAndStatus(search, status, pageable);
        } else if (hasSearch) {
            entregas = entregaRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            entregas = entregaRepository.findByStatus(status, pageable);
        } else {
            entregas = entregaRepository.findAll(pageable);
        }

        return enrichPage(entregas);
    }

    @Override
    public EntregaResponse findById(String id) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));
        Cliente cliente = StringUtils.hasText(entrega.getClienteId())
                ? clienteRepository.findById(entrega.getClienteId()).orElse(null)
                : null;
        return EntregaResponse.from(entrega, cliente);
    }

    @Override
    public EntregaResponse create(EntregaRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));

        Entrega entrega = Entrega.builder()
                .clienteId(request.getClienteId())
                .direccionEnvio(request.getDireccionEnvio())
                .status(request.getStatus() != null ? request.getStatus() : Entrega.StatusEntrega.ACTIVO)
                .build();

        Entrega saved = entregaRepository.save(entrega);
        log.info("Entrega creada con id: {} para cliente: {}", saved.getId(), cliente.getNombre());
        return EntregaResponse.from(saved, cliente);
    }

    @Override
    public EntregaResponse update(String id, EntregaRequest request) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));

        entrega.setClienteId(request.getClienteId());
        entrega.setDireccionEnvio(request.getDireccionEnvio());
        if (request.getStatus() != null) {
            entrega.setStatus(request.getStatus());
        }

        Entrega updated = entregaRepository.save(entrega);
        log.info("Entrega actualizada con id: {}", updated.getId());
        return EntregaResponse.from(updated, cliente);
    }

    @Override
    public void delete(String id) {
        if (!entregaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Entrega", "id", id);
        }
        entregaRepository.deleteById(id);
        log.info("Entrega eliminada con id: {}", id);
    }

    @Override
    public PagedResponse<EntregaResponse> findByClienteId(String clienteId, int page, int size) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente", "id", clienteId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Entrega> pageEntregas = entregaRepository.findByClienteId(clienteId, pageable);
        return enrichPage(pageEntregas);
    }

    private PagedResponse<EntregaResponse> enrichPage(Page<Entrega> page) {
        if (page.isEmpty()) {
            return PagedResponse.of(Page.empty(page.getPageable()));
        }

        Set<String> clienteIds = page.getContent().stream()
                .map(Entrega::getClienteId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Cliente> clienteMap = clienteIds.isEmpty()
                ? Collections.emptyMap()
                : clienteRepository.findAllById(clienteIds).stream()
                        .collect(Collectors.toMap(Cliente::getId, c -> c));

        List<EntregaResponse> enriched = page.getContent().stream()
                .map(e -> EntregaResponse.from(e, clienteMap.get(e.getClienteId())))
                .collect(Collectors.toList());

        Page<EntregaResponse> responsePage = new PageImpl<>(
                enriched, page.getPageable(), page.getTotalElements());

        return PagedResponse.of(responsePage);
    }
}
