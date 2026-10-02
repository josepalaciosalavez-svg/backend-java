package com.liverpool.backend.repository;

import com.liverpool.backend.model.Entrega;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EntregaRepository extends MongoRepository<Entrega, String> {

    List<Entrega> findByDatosEntregaId(String datosEntregaId);

    Page<Entrega> findByStatusPedido(Entrega.StatusPedido statusPedido, Pageable pageable);

    Page<Entrega> findByDatosEntregaId(String datosEntregaId, Pageable pageable);

    // Permite filtrar todas las entregas de un cliente dado su conjunto de datosEntregaIds
    Page<Entrega> findByDatosEntregaIdIn(Collection<String> datosEntregaIds, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'codigo_producto': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'status_pedido':   { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<Entrega> searchByTerm(String term, Pageable pageable);

    @Query("{ '$and': [ " +
           "{ 'status_pedido': ?1 }, " +
           "{ '$or': [ " +
           "{ 'codigo_producto': { '$regex': ?0, '$options': 'i' } } " +
           "] ] }")
    Page<Entrega> searchByTermAndStatus(String term, Entrega.StatusPedido statusPedido, Pageable pageable);
}
