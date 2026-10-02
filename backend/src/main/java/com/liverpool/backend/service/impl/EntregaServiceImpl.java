package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.EntregaRequest;
import com.liverpool.backend.dto.response.EntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.DatosEntrega;
import com.liverpool.backend.model.Entrega;
import com.liverpool.backend.repository.DatosEntregaRepository;
import com.liverpool.backend.repository.EntregaRepository;
import com.liverpool.backend.service.EntregaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final DatosEntregaRepository datosEntregaRepository;

    @Override
    public PagedResponse<EntregaResponse> findAll(int page, int size, String sortBy,
                                                   String sortDir, String search,
                                                   Entrega.StatusPedido statusPedido) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Entrega> entregas;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = statusPedido != null;

        if (hasSearch && hasStatus) {
            entregas = entregaRepository.searchByTermAndStatus(search, statusPedido, pageable);
        } else if (hasSearch) {
            entregas = entregaRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            entregas = entregaRepository.findByStatusPedido(statusPedido, pageable);
        } else {
            entregas = entregaRepository.findAll(pageable);
        }

        return PagedResponse.of(entregas.map(EntregaResponse::from));
    }

    @Override
    public EntregaResponse findById(String id) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));
        return EntregaResponse.from(entrega);
    }

    @Override
    public EntregaResponse create(EntregaRequest request) {
        // Validar que el datosEntregaId exista si se proporcionó
        DatosEntrega datosEntrega = resolverDatosEntrega(request.getDatosEntregaId());

        Entrega entrega = Entrega.builder()
                .codigoProducto(request.getCodigoProducto())
                .cantidad(request.getCantidad())
                .precio(request.getPrecio())
                .statusPedido(request.getStatusPedido() != null
                        ? request.getStatusPedido()
                        : Entrega.StatusPedido.PENDIENTE)
                .datosEntregaId(request.getDatosEntregaId())
                .build();

        Entrega saved = entregaRepository.save(entrega);
        log.info("Entrega creada con id: {} para cliente: {}", saved.getId(),
                datosEntrega != null ? datosEntrega.getClienteId() : "sin asignar");
        return EntregaResponse.from(saved, datosEntrega);
    }

    @Override
    public EntregaResponse update(String id, EntregaRequest request) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", id));

        // Validar que el nuevo datosEntregaId exista si cambió
        DatosEntrega datosEntrega = resolverDatosEntrega(request.getDatosEntregaId());

        entrega.setCodigoProducto(request.getCodigoProducto());
        entrega.setCantidad(request.getCantidad());
        entrega.setPrecio(request.getPrecio());
        entrega.setDatosEntregaId(request.getDatosEntregaId());
        if (request.getStatusPedido() != null) {
            entrega.setStatusPedido(request.getStatusPedido());
        }

        Entrega updated = entregaRepository.save(entrega);
        log.info("Entrega actualizada con id: {}", updated.getId());
        return EntregaResponse.from(updated, datosEntrega);
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
    public PagedResponse<EntregaResponse> findByClienteId(String clienteId, int page, int size,
                                                          String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Primero se obtienen los IDs de DatosEntrega que pertenecen al cliente
        List<String> datosEntregaIds = datosEntregaRepository.findByClienteId(clienteId)
                .stream()
                .map(de -> de.getId())
                .collect(Collectors.toList());

        if (datosEntregaIds.isEmpty()) {
            return PagedResponse.of(Page.empty(pageable));
        }

        return PagedResponse.of(
                entregaRepository.findByDatosEntregaIdIn(datosEntregaIds, pageable)
                        .map(EntregaResponse::from)
        );
    }

    @Override
    public PagedResponse<EntregaResponse> findByDatosEntregaId(String datosEntregaId, int page, int size) {
        // Valida que la dirección de entrega exista antes de buscar
        if (!datosEntregaRepository.existsById(datosEntregaId)) {
            throw new ResourceNotFoundException("DatosEntrega", "id", datosEntregaId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.of(
                entregaRepository.findByDatosEntregaId(datosEntregaId, pageable)
                        .map(EntregaResponse::from)
        );
    }

    // ─── helpers privados ─────────────────────────────────────────────────────


    /**
     * Busca el DatosEntrega correspondiente al ID dado.
     * Si el ID no es nulo y no existe en la base de datos, lanza ResourceNotFoundException.
     * Si el ID es nulo (entrega sin dirección asignada aún), retorna null.
     */
    private DatosEntrega resolverDatosEntrega(String datosEntregaId) {
        if (!StringUtils.hasText(datosEntregaId)) {
            return null;
        }
        return datosEntregaRepository.findById(datosEntregaId)
                .orElseThrow(() -> new ResourceNotFoundException("DatosEntrega", "id", datosEntregaId));
    }
}
