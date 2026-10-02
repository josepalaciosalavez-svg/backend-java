package com.liverpool.backend.migration;

import com.liverpool.backend.model.Cliente;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.domain.Sort;

@Slf4j
@ChangeUnit(id = "V001-create-clientes-collection", order = "001", author = "dev-team")
public class V001ClienteMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        // Crear colección si no existe
        if (!mongoTemplate.collectionExists(Cliente.class)) {
            mongoTemplate.createCollection(Cliente.class);
            log.info("Colección 'clientes' creada");
        }

        // Índices
        IndexOperations indexOps = mongoTemplate.indexOps(Cliente.class);
        try {
            indexOps.dropIndex("email");
        } catch (Exception ignored) {
            // Ignorar si no existía el índice por defecto
        }
        indexOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique().named("idx_email_unique"));
        indexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC).named("idx_status"));
        indexOps.ensureIndex(new Index().on("created_at", Sort.Direction.DESC).named("idx_created_at"));
        log.info("Índices de 'clientes' aplicados");

        // Seeders
        if (mongoTemplate.estimatedCount(Cliente.class) == 0) {
            seedClientes(mongoTemplate);
        }
    }

    private void seedClientes(MongoTemplate mongoTemplate) {
        mongoTemplate.save(Cliente.builder()
                .nombre("Carlos")
                .apellidoPaterno("Ramírez")
                .apellidoMaterno("Herrera")
                .email("carlos.ramirez@correo.com")
                .status(Cliente.StatusCliente.ACTIVO)
                .build());

        mongoTemplate.save(Cliente.builder()
                .nombre("María")
                .apellidoPaterno("González")
                .apellidoMaterno("López")
                .email("maria.gonzalez@correo.com")
                .status(Cliente.StatusCliente.ACTIVO)
                .build());

        mongoTemplate.save(Cliente.builder()
                .nombre("Jorge")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Soto")
                .email("jorge.mendoza@correo.com")
                .status(Cliente.StatusCliente.INACTIVO)
                .build());

        mongoTemplate.save(Cliente.builder()
                .nombre("Ana")
                .apellidoPaterno("Torres")
                .apellidoMaterno("Vega")
                .email("ana.torres@correo.com")
                .status(Cliente.StatusCliente.ACTIVO)
                .build());

        mongoTemplate.save(Cliente.builder()
                .nombre("Luis")
                .apellidoPaterno("Castillo")
                .apellidoMaterno("Fuentes")
                .email("luis.castillo@correo.com")
                .status(Cliente.StatusCliente.ACTIVO)
                .build());

        log.info("5 clientes de prueba insertados");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Cliente.class);
        log.warn("Rollback: colección 'clientes' eliminada");
    }
}
