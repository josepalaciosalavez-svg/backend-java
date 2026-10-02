package com.liverpool.backend.migration;

import com.liverpool.backend.model.Entrega;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import java.math.BigDecimal;

@Slf4j
@ChangeUnit(id = "V003-create-entregas-collection", order = "003", author = "dev-team")
public class V003EntregaMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(Entrega.class)) {
            mongoTemplate.createCollection(Entrega.class);
            log.info("Colección 'entregas' creada");
        }

        IndexOperations indexOps = mongoTemplate.indexOps(Entrega.class);
        indexOps.ensureIndex(new Index().on("codigo_producto", Sort.Direction.ASC).named("idx_codigo_producto"));
        indexOps.ensureIndex(new Index().on("status_pedido", Sort.Direction.ASC).named("idx_status_pedido"));
        indexOps.ensureIndex(new Index().on("datos_entrega_id", Sort.Direction.ASC).named("idx_datos_entrega_id"));
        indexOps.ensureIndex(new Index().on("created_at", Sort.Direction.DESC).named("idx_created_at"));
        log.info("Índices de 'entregas' aplicados");

        if (mongoTemplate.estimatedCount(Entrega.class) == 0) {
            seedEntregas(mongoTemplate);
        }
    }

    private void seedEntregas(MongoTemplate mongoTemplate) {
        mongoTemplate.save(Entrega.builder()
                .codigoProducto("LIV-ELEC-001")
                .cantidad(2)
                .precio(new BigDecimal("4599.00"))
                .statusPedido(Entrega.StatusPedido.ENTREGADO)
                .build());

        mongoTemplate.save(Entrega.builder()
                .codigoProducto("LIV-ROPA-042")
                .cantidad(3)
                .precio(new BigDecimal("899.50"))
                .statusPedido(Entrega.StatusPedido.ENVIADO)
                .build());

        mongoTemplate.save(Entrega.builder()
                .codigoProducto("LIV-HOGAR-117")
                .cantidad(1)
                .precio(new BigDecimal("12300.00"))
                .statusPedido(Entrega.StatusPedido.PROCESANDO)
                .build());

        mongoTemplate.save(Entrega.builder()
                .codigoProducto("LIV-DEPO-085")
                .cantidad(5)
                .precio(new BigDecimal("350.00"))
                .statusPedido(Entrega.StatusPedido.PENDIENTE)
                .build());

        mongoTemplate.save(Entrega.builder()
                .codigoProducto("LIV-JUGU-023")
                .cantidad(1)
                .precio(new BigDecimal("1250.75"))
                .statusPedido(Entrega.StatusPedido.CANCELADO)
                .build());

        log.info("5 entregas/pedidos de prueba insertados");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Entrega.class);
        log.warn("Rollback: colección 'entregas' eliminada");
    }
}
