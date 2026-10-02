package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.EntregaRequest;
import com.liverpool.backend.dto.response.EntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.Entrega;

public interface EntregaService {

    PagedResponse<EntregaResponse> findAll(int page, int size, String sortBy, String sortDir,
                                           String search, Entrega.StatusEntrega status);

    EntregaResponse findById(String id);

    EntregaResponse create(EntregaRequest request);

    EntregaResponse update(String id, EntregaRequest request);

    void delete(String id);

    PagedResponse<EntregaResponse> findByClienteId(String clienteId, int page, int size);
}
