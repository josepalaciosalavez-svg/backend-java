package com.liverpool.backend.repository;

import com.liverpool.backend.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {

    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Cliente> findByStatus(Cliente.StatusCliente status, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'nombre': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'apellido_paterno': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'apellido_materno': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<Cliente> searchByTerm(String term, Pageable pageable);

    @Query("{ '$and': [ " +
           "{ 'status': ?1 }, " +
           "{ '$or': [ " +
           "{ 'nombre': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'apellido_paterno': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'apellido_materno': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?0, '$options': 'i' } } " +
           "] ] }")
    Page<Cliente> searchByTermAndStatus(String term, Cliente.StatusCliente status, Pageable pageable);
}
