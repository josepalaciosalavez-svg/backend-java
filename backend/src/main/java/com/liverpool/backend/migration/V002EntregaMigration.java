package com.liverpool.backend.migration;

import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Entrega;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Slf4j
@ChangeUnit(id = "V002-create-entregas-collection", order = "002", author = "dev-team")
public class V002EntregaMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(Entrega.class)) {
            mongoTemplate.createCollection(Entrega.class);
            log.info("Colección 'entregas' creada");
        }

        IndexOperations indexOps = mongoTemplate.indexOps(Entrega.class);
        try {
            indexOps.dropIndex("cliente_id");
        } catch (Exception ignored) {
        }
        indexOps.ensureIndex(new Index().on("cliente_id", Sort.Direction.ASC).named("idx_cliente_id"));
        indexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC).named("idx_status"));
        indexOps.ensureIndex(new Index().on("created_at", Sort.Direction.DESC).named("idx_created_at"));
        log.info("Índices de 'entregas' aplicados");

        if (mongoTemplate.estimatedCount(Entrega.class) == 0) {
            seedEntregas(mongoTemplate);
        }
    }

    private void seedEntregas(MongoTemplate mongoTemplate) {
        List<Cliente> clientes = mongoTemplate.find(new Query().limit(3), Cliente.class);

        if (clientes.isEmpty()) {
            log.warn("No hay clientes disponibles para crear entregas de prueba");
            return;
        }

        String[] direcciones = {
            "Av. Insurgentes Sur 1602, Col. Crédito Constructor, CDMX",
            "Calle Morelos 45, Col. Centro, Guadalajara, Jalisco",
            "Blvd. Adolfo López Mateos 2000, Monterrey, Nuevo León",
            "Av. Universidad 3000, Puebla, Puebla"
        };

        Entrega.StatusEntrega[] statuses = {
            Entrega.StatusEntrega.ACTIVO,
            Entrega.StatusEntrega.ACTIVO,
            Entrega.StatusEntrega.ACTIVO,
            Entrega.StatusEntrega.INACTIVO
        };

        for (int i = 0; i < direcciones.length; i++) {
            Cliente cliente = clientes.get(i % clientes.size());
            mongoTemplate.save(Entrega.builder()
                    .clienteId(cliente.getId())
                    .direccionEnvio(direcciones[i])
                    .status(statuses[i])
                    .build());
        }

        log.info("{} entregas de prueba insertadas ligadas a clientes", direcciones.length);
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Entrega.class);
        log.warn("Rollback: colección 'entregas' eliminada");
    }
}
