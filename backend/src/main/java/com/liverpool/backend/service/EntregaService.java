package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.EntregaRequest;
import com.liverpool.backend.dto.response.EntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.Entrega;

public interface EntregaService {

    PagedResponse<EntregaResponse> findAll(int page, int size, String sortBy, String sortDir,
                                           String search, Entrega.StatusPedido statusPedido);

    EntregaResponse findById(String id);

    // Consulta todos los pedidos de un cliente (vía sus DatosEntrega)
    PagedResponse<EntregaResponse> findByClienteId(String clienteId, int page, int size,
                                                   String sortBy, String sortDir);

    // Consulta pedidos de una dirección de entrega específica
    PagedResponse<EntregaResponse> findByDatosEntregaId(String datosEntregaId, int page, int size);

    EntregaResponse create(EntregaRequest request);

    EntregaResponse update(String id, EntregaRequest request);

    void delete(String id);
}
