package com.liverpool.backend.repository;

import com.liverpool.backend.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    Page<Pedido> findByStatusPedido(Pedido.StatusPedido statusPedido, Pageable pageable);

    Page<Pedido> findByEntregaId(String entregaId, Pageable pageable);

    Page<Pedido> findByEntregaIdIn(Collection<String> entregaIds, Pageable pageable);

    @Query("{ 'codigo_producto': { '$regex': ?0, '$options': 'i' } }")
    Page<Pedido> searchByTerm(String term, Pageable pageable);

    @Query("{ '$and': [ " +
           "{ 'status_pedido': ?1 }, " +
           "{ 'codigo_producto': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<Pedido> searchByTermAndStatus(String term, Pedido.StatusPedido statusPedido, Pageable pageable);
}
