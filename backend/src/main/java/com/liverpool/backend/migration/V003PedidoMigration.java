package com.liverpool.backend.migration;

import com.liverpool.backend.model.Entrega;
import com.liverpool.backend.model.Pedido;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@ChangeUnit(id = "V003-create-pedidos-collection", order = "003", author = "dev-team")
public class V003PedidoMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(Pedido.class)) {
            mongoTemplate.createCollection(Pedido.class);
            log.info("Colección 'pedidos' creada");
        }

        IndexOperations indexOps = mongoTemplate.indexOps(Pedido.class);
        indexOps.ensureIndex(new Index().on("codigo_producto", Sort.Direction.ASC).named("idx_codigo_producto"));
        indexOps.ensureIndex(new Index().on("status_pedido", Sort.Direction.ASC).named("idx_status_pedido"));
        indexOps.ensureIndex(new Index().on("entrega_id", Sort.Direction.ASC).named("idx_entrega_id"));
        indexOps.ensureIndex(new Index().on("created_at", Sort.Direction.DESC).named("idx_created_at"));
        log.info("Índices de 'pedidos' aplicados");

        if (mongoTemplate.estimatedCount(Pedido.class) == 0) {
            seedPedidos(mongoTemplate);
        }
    }

    private void seedPedidos(MongoTemplate mongoTemplate) {
        List<Entrega> entregas = mongoTemplate.find(new Query().limit(4), Entrega.class);

        String e0 = entregas.size() > 0 ? entregas.get(0).getId() : null;
        String e1 = entregas.size() > 1 ? entregas.get(1).getId() : e0;
        String e2 = entregas.size() > 2 ? entregas.get(2).getId() : e0;

        mongoTemplate.save(Pedido.builder()
                .codigoProducto("LIV-ELEC-001")
                .cantidad(2)
                .precio(new BigDecimal("4599.00"))
                .statusPedido(Pedido.StatusPedido.ENTREGADO)
                .entregaId(e0)
                .build());

        mongoTemplate.save(Pedido.builder()
                .codigoProducto("LIV-ROPA-042")
                .cantidad(3)
                .precio(new BigDecimal("899.50"))
                .statusPedido(Pedido.StatusPedido.ENVIADO)
                .entregaId(e1)
                .build());

        mongoTemplate.save(Pedido.builder()
                .codigoProducto("LIV-HOGAR-117")
                .cantidad(1)
                .precio(new BigDecimal("12300.00"))
                .statusPedido(Pedido.StatusPedido.PROCESANDO)
                .entregaId(e2)
                .build());

        mongoTemplate.save(Pedido.builder()
                .codigoProducto("LIV-DEP-505")
                .cantidad(1)
                .precio(new BigDecimal("3299.99"))
                .statusPedido(Pedido.StatusPedido.PENDIENTE)
                .entregaId(e0)
                .build());

        mongoTemplate.save(Pedido.builder()
                .codigoProducto("LIV-BELLEZA-088")
                .cantidad(4)
                .precio(new BigDecimal("450.00"))
                .statusPedido(Pedido.StatusPedido.CANCELADO)
                .entregaId(e1)
                .build());

        log.info("5 pedidos de prueba insertados ligados a entregas");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Pedido.class);
        log.warn("Rollback: colección 'pedidos' eliminada");
    }
}
