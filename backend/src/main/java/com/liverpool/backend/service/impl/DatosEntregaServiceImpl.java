package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.DatosEntregaRequest;
import com.liverpool.backend.dto.response.DatosEntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.DatosEntrega;
import com.liverpool.backend.repository.ClienteRepository;
import com.liverpool.backend.repository.DatosEntregaRepository;
import com.liverpool.backend.service.DatosEntregaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatosEntregaServiceImpl implements DatosEntregaService {

    private final DatosEntregaRepository datosEntregaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public PagedResponse<DatosEntregaResponse> findAll(int page, int size, String sortBy,
                                                        String sortDir, String search,
                                                        DatosEntrega.StatusEntrega status) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DatosEntrega> datos;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = status != null;

        if (hasSearch && hasStatus) {
            datos = datosEntregaRepository.searchByTermAndStatus(search, status, pageable);
        } else if (hasSearch) {
            datos = datosEntregaRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            datos = datosEntregaRepository.findByStatus(status, pageable);
        } else {
            datos = datosEntregaRepository.findAll(pageable);
        }

        return PagedResponse.of(datos.map(DatosEntregaResponse::from));
    }

    @Override
    public DatosEntregaResponse findById(String id) {
        DatosEntrega datosEntrega = datosEntregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosEntrega", "id", id));
        return DatosEntregaResponse.from(datosEntrega);
    }

    @Override
    public DatosEntregaResponse create(DatosEntregaRequest request) {
        // Verificar que el cliente exista
        if (!clienteRepository.existsById(request.getClienteId())) {
            throw new ResourceNotFoundException("Cliente", "id", request.getClienteId());
        }

        DatosEntrega datosEntrega = DatosEntrega.builder()
                .clienteId(request.getClienteId())
                .direccionEnvio(request.getDireccionEnvio())
                .status(request.getStatus() != null ? request.getStatus() : DatosEntrega.StatusEntrega.PENDIENTE)
                .build();

        DatosEntrega saved = datosEntregaRepository.save(datosEntrega);
        log.info("DatosEntrega creado con id: {}", saved.getId());
        return DatosEntregaResponse.from(saved);
    }

    @Override
    public DatosEntregaResponse update(String id, DatosEntregaRequest request) {
        DatosEntrega datosEntrega = datosEntregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatosEntrega", "id", id));

        if (!clienteRepository.existsById(request.getClienteId())) {
            throw new ResourceNotFoundException("Cliente", "id", request.getClienteId());
        }

        datosEntrega.setClienteId(request.getClienteId());
        datosEntrega.setDireccionEnvio(request.getDireccionEnvio());
        if (request.getStatus() != null) {
            datosEntrega.setStatus(request.getStatus());
        }

        DatosEntrega updated = datosEntregaRepository.save(datosEntrega);
        log.info("DatosEntrega actualizado con id: {}", updated.getId());
        return DatosEntregaResponse.from(updated);
    }

    @Override
    public void delete(String id) {
        if (!datosEntregaRepository.existsById(id)) {
            throw new ResourceNotFoundException("DatosEntrega", "id", id);
        }
        datosEntregaRepository.deleteById(id);
        log.info("DatosEntrega eliminado con id: {}", id);
    }
}
