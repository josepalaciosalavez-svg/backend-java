package com.liverpool.backend.repository;

import com.liverpool.backend.model.DatosEntrega;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatosEntregaRepository extends MongoRepository<DatosEntrega, String> {

    List<DatosEntrega> findByClienteId(String clienteId);

    Page<DatosEntrega> findByStatus(DatosEntrega.StatusEntrega status, Pageable pageable);

    Page<DatosEntrega> findByClienteId(String clienteId, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'cliente_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'direccion_envio': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<DatosEntrega> searchByTerm(String term, Pageable pageable);

    @Query("{ '$and': [ " +
           "{ 'status': ?1 }, " +
           "{ '$or': [ " +
           "{ 'cliente_id': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'direccion_envio': { '$regex': ?0, '$options': 'i' } } " +
           "] ] }")
    Page<DatosEntrega> searchByTermAndStatus(String term, DatosEntrega.StatusEntrega status, Pageable pageable);
}
