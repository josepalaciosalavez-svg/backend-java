package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.DatosEntregaRequest;
import com.liverpool.backend.dto.response.DatosEntregaResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.DatosEntrega;

public interface DatosEntregaService {

    PagedResponse<DatosEntregaResponse> findAll(int page, int size, String sortBy, String sortDir,
                                                 String search, DatosEntrega.StatusEntrega status);

    DatosEntregaResponse findById(String id);

    DatosEntregaResponse create(DatosEntregaRequest request);

    DatosEntregaResponse update(String id, DatosEntregaRequest request);

    void delete(String id);
}
