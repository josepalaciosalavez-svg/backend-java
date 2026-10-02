package com.liverpool.backend.repository;

import com.liverpool.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Usuario> findByStatus(Usuario.StatusUsuario status, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'nombre': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'email': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    Page<Usuario> searchByTerm(String term, Pageable pageable);
}
