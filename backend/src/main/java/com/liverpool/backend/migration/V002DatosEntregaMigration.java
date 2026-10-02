package com.liverpool.backend.migration;

import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.DatosEntrega;
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
@ChangeUnit(id = "V002-create-datos-entrega-collection", order = "002", author = "dev-team")
public class V002DatosEntregaMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(DatosEntrega.class)) {
            mongoTemplate.createCollection(DatosEntrega.class);
            log.info("Colección 'datos_entrega' creada");
        }

        IndexOperations indexOps = mongoTemplate.indexOps(DatosEntrega.class);
        try {
            indexOps.dropIndex("cliente_id");
        } catch (Exception ignored) {
            // Ignorar si no existía
        }
        indexOps.ensureIndex(new Index().on("cliente_id", Sort.Direction.ASC).named("idx_cliente_id"));
        indexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC).named("idx_status"));
        indexOps.ensureIndex(new Index().on("created_at", Sort.Direction.DESC).named("idx_created_at"));
        log.info("Índices de 'datos_entrega' aplicados");

        if (mongoTemplate.estimatedCount(DatosEntrega.class) == 0) {
            seedDatosEntrega(mongoTemplate);
        }
    }

    private void seedDatosEntrega(MongoTemplate mongoTemplate) {
        // Obtener los primeros 3 clientes para relacionar
        List<Cliente> clientes = mongoTemplate.find(new Query().limit(3), Cliente.class);

        if (clientes.isEmpty()) {
            log.warn("No hay clientes disponibles para crear datos de entrega de prueba");
            return;
        }

        String[] direcciones = {
            "Av. Insurgentes Sur 1602, Col. Crédito Constructor, CDMX",
            "Calle Morelos 45, Col. Centro, Guadalajara, Jalisco",
            "Blvd. Adolfo López Mateos 2000, Monterrey, Nuevo León",
            "Av. Universidad 3000, Puebla, Puebla"
        };

        DatosEntrega.StatusEntrega[] statuses = {
            DatosEntrega.StatusEntrega.PENDIENTE,
            DatosEntrega.StatusEntrega.EN_PROCESO,
            DatosEntrega.StatusEntrega.ENTREGADO
        };

        for (int i = 0; i < Math.min(3, clientes.size()); i++) {
            mongoTemplate.save(DatosEntrega.builder()
                    .clienteId(clientes.get(i).getId())
                    .direccionEnvio(direcciones[i])
                    .status(statuses[i])
                    .build());
        }

        // Agregar uno extra con el primer cliente
        if (!clientes.isEmpty()) {
            mongoTemplate.save(DatosEntrega.builder()
                    .clienteId(clientes.get(0).getId())
                    .direccionEnvio(direcciones[3])
                    .status(DatosEntrega.StatusEntrega.PENDIENTE)
                    .build());
        }

        log.info("Datos de entrega de prueba insertados");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(DatosEntrega.class);
        log.warn("Rollback: colección 'datos_entrega' eliminada");
    }
}
