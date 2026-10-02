package com.liverpool.backend.repository;

import com.liverpool.backend.model.Entrega;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaRepository extends MongoRepository<Entrega, String> {

    List<Entrega> findByClienteId(String clienteId);

    List<Entrega> findAllById(Iterable<String> ids);

    Page<Entrega> findByStatus(Entrega.StatusEntrega status, Pageable pageable);

    Page<Entrega> findByClienteId(String clienteId, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'cliente_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'direccion_envio': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<Entrega> searchByTerm(String term, Pageable pageable);

    @Query("{ '$and': [ " +
           "{ 'status': ?1 }, " +
           "{ '$or': [ " +
           "{ 'cliente_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'direccion_envio': { '$regex': ?0, '$options': 'i' } } " +
           "] ] }")
    Page<Entrega> searchByTermAndStatus(String term, Entrega.StatusEntrega status, Pageable pageable);
}
