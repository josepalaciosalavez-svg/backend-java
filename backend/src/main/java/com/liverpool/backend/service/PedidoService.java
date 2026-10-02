package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.PedidoRequest;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.dto.response.PedidoResponse;
import com.liverpool.backend.model.Pedido;

public interface PedidoService {

    PagedResponse<PedidoResponse> findAll(int page, int size, String sortBy, String sortDir,
                                          String search, Pedido.StatusPedido statusPedido);

    PedidoResponse findById(String id);

    PedidoResponse create(PedidoRequest request);

    PedidoResponse update(String id, PedidoRequest request);

    void delete(String id);

    PagedResponse<PedidoResponse> findByClienteId(String clienteId, int page, int size,
                                                  String sortBy, String sortDir);

    PagedResponse<PedidoResponse> findByEntregaId(String entregaId, int page, int size);
}
