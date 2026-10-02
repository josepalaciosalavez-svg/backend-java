package com.liverpool.backend.service;

import com.liverpool.backend.dto.request.ClienteRequest;
import com.liverpool.backend.dto.response.ClienteResponse;
import com.liverpool.backend.dto.response.PagedResponse;
import com.liverpool.backend.model.Cliente;

public interface ClienteService {

    PagedResponse<ClienteResponse> findAll(int page, int size, String sortBy, String sortDir,
                                           String search, Cliente.StatusCliente status);

    ClienteResponse findById(String id);

    ClienteResponse create(ClienteRequest request);

    ClienteResponse update(String id, ClienteRequest request);

    void delete(String id);
}
