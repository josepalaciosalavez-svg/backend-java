package com.liverpool.backend.service.impl;

import com.liverpool.backend.dto.request.PedidoRequest;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.PedidoResponse;
import com.liverpool.backend.exception.ResourceNotFoundException;
import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Entrega;
import com.liverpool.backend.model.Pedido;
import com.liverpool.backend.repository.ClienteRepository;
import com.liverpool.backend.repository.EntregaRepository;
import com.liverpool.backend.repository.PedidoRepository;
import com.liverpool.backend.service.PedidoService;
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
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public PagedResponse<PedidoResponse> findAll(int page, int size, String sortBy,
                                                 String sortDir, String search,
                                                 Pedido.StatusPedido statusPedido) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Pedido> pedidos;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasStatus = statusPedido != null;

        if (hasSearch && hasStatus) {
            pedidos = pedidoRepository.searchByTermAndStatus(search, statusPedido, pageable);
        } else if (hasSearch) {
            pedidos = pedidoRepository.searchByTerm(search, pageable);
        } else if (hasStatus) {
            pedidos = pedidoRepository.findByStatusPedido(statusPedido, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        return enrichPage(pedidos);
    }

    @Override
    public PedidoResponse findById(String id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        Entrega entrega = pedido.getEntregaId() != null
                ? entregaRepository.findById(pedido.getEntregaId()).orElse(null)
                : null;

        Cliente cliente = (entrega != null && StringUtils.hasText(entrega.getClienteId()))
                ? clienteRepository.findById(entrega.getClienteId()).orElse(null)
                : null;

        return PedidoResponse.from(
                pedido,
                entrega != null ? entrega.getDireccionEnvio() : null,
                entrega != null && entrega.getStatus() != null ? entrega.getStatus().name() : null,
                entrega != null ? entrega.getClienteId() : null,
                cliente
        );
    }

    @Override
    public PedidoResponse create(PedidoRequest request) {
        Entrega entrega = resolverEntrega(request.getEntregaId());
        Cliente cliente = (entrega != null && StringUtils.hasText(entrega.getClienteId()))
                ? clienteRepository.findById(entrega.getClienteId()).orElse(null)
                : null;

        Pedido pedido = Pedido.builder()
                .codigoProducto(request.getCodigoProducto())
                .cantidad(request.getCantidad())
                .precio(request.getPrecio())
                .statusPedido(request.getStatusPedido() != null
                        ? request.getStatusPedido()
                        : Pedido.StatusPedido.PENDIENTE)
                .entregaId(request.getEntregaId())
                .build();

        Pedido saved = pedidoRepository.save(pedido);
        log.info("Pedido creado con id: {} para cliente: {}", saved.getId(),
                cliente != null ? cliente.getNombre() : "sin cliente");

        return PedidoResponse.from(
                saved,
                entrega != null ? entrega.getDireccionEnvio() : null,
                entrega != null && entrega.getStatus() != null ? entrega.getStatus().name() : null,
                entrega != null ? entrega.getClienteId() : null,
                cliente
        );
    }

    @Override
    public PedidoResponse update(String id, PedidoRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        Entrega entrega = resolverEntrega(request.getEntregaId());
        Cliente cliente = (entrega != null && StringUtils.hasText(entrega.getClienteId()))
                ? clienteRepository.findById(entrega.getClienteId()).orElse(null)
                : null;

        pedido.setCodigoProducto(request.getCodigoProducto());
        pedido.setCantidad(request.getCantidad());
        pedido.setPrecio(request.getPrecio());
        pedido.setEntregaId(request.getEntregaId());
        if (request.getStatusPedido() != null) {
            pedido.setStatusPedido(request.getStatusPedido());
        }

        Pedido updated = pedidoRepository.save(pedido);
        log.info("Pedido actualizado con id: {}", updated.getId());

        return PedidoResponse.from(
                updated,
                entrega != null ? entrega.getDireccionEnvio() : null,
                entrega != null && entrega.getStatus() != null ? entrega.getStatus().name() : null,
                entrega != null ? entrega.getClienteId() : null,
                cliente
        );
    }

    @Override
    public void delete(String id) {
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido", "id", id);
        }
        pedidoRepository.deleteById(id);
        log.info("Pedido eliminado con id: {}", id);
    }

    @Override
    public PagedResponse<PedidoResponse> findByClienteId(String clienteId, int page, int size,
                                                          String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<String> entregaIds = entregaRepository.findByClienteId(clienteId)
                .stream()
                .map(Entrega::getId)
                .collect(Collectors.toList());

        if (entregaIds.isEmpty()) {
            return PagedResponse.of(Page.empty(pageable));
        }

        Page<Pedido> pagePedidos = pedidoRepository.findByEntregaIdIn(entregaIds, pageable);
        return enrichPage(pagePedidos);
    }

    @Override
    public PagedResponse<PedidoResponse> findByEntregaId(String entregaId, int page, int size) {
        if (!entregaRepository.existsById(entregaId)) {
            throw new ResourceNotFoundException("Entrega", "id", entregaId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pedido> pagePedidos = pedidoRepository.findByEntregaId(entregaId, pageable);
        return enrichPage(pagePedidos);
    }

    // ─── helpers privados ─────────────────────────────────────────────────────

    private PagedResponse<PedidoResponse> enrichPage(Page<Pedido> page) {
        if (page.isEmpty()) {
            return PagedResponse.of(Page.empty(page.getPageable()));
        }

        Set<String> entregaIds = page.getContent().stream()
                .map(Pedido::getEntregaId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Entrega> entregaMap = entregaIds.isEmpty()
                ? Collections.emptyMap()
                : entregaRepository.findAllById(entregaIds).stream()
                        .collect(Collectors.toMap(Entrega::getId, e -> e));

        Set<String> clienteIds = entregaMap.values().stream()
                .map(Entrega::getClienteId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, Cliente> clienteMap = clienteIds.isEmpty()
                ? Collections.emptyMap()
                : clienteRepository.findAllById(clienteIds).stream()
                        .collect(Collectors.toMap(Cliente::getId, c -> c));

        List<PedidoResponse> enriched = page.getContent().stream().map(pedido -> {
            Entrega entrega = entregaMap.get(pedido.getEntregaId());
            Cliente cliente = (entrega != null && entrega.getClienteId() != null)
                    ? clienteMap.get(entrega.getClienteId())
                    : null;
            return PedidoResponse.from(
                    pedido,
                    entrega != null ? entrega.getDireccionEnvio() : null,
                    entrega != null && entrega.getStatus() != null ? entrega.getStatus().name() : null,
                    entrega != null ? entrega.getClienteId() : null,
                    cliente
            );
        }).collect(Collectors.toList());

        Page<PedidoResponse> responsePage = new PageImpl<>(
                enriched, page.getPageable(), page.getTotalElements());

        return PagedResponse.of(responsePage);
    }

    private Entrega resolverEntrega(String entregaId) {
        if (!StringUtils.hasText(entregaId)) {
            return null;
        }
        return entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", "id", entregaId));
    }
}
