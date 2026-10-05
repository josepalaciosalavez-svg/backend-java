package com.liverpool.backend.migration;

import com.liverpool.backend.model.Usuario;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@ChangeUnit(id = "V004-create-usuarios-collection", order = "004", author = "dev-team")
public class V004UsuarioMigration {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(Usuario.class)) {
            mongoTemplate.createCollection(Usuario.class);
            log.info("Colección 'usuarios' creada");
        }

        IndexOperations indexOps = mongoTemplate.indexOps(Usuario.class);
        try {
            indexOps.dropIndex("email");
        } catch (Exception ignored) {
            // Ignorar si no existía el índice por defecto
        }
        indexOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique().named("idx_email_unique"));
        indexOps.ensureIndex(new Index().on("status", Sort.Direction.ASC).named("idx_status"));
        log.info("Índices de 'usuarios' aplicados");

        if (mongoTemplate.estimatedCount(Usuario.class) == 0) {
            seedUsuarios(mongoTemplate);
        }
    }

    private void seedUsuarios(MongoTemplate mongoTemplate) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Admin principal
        Set<Usuario.Rol> rolesAdmin = new HashSet<>();
        rolesAdmin.add(Usuario.Rol.ROLE_ADMIN);

        mongoTemplate.save(Usuario.builder()
                .nombre("Administrador Sistema")
                .email("admin@liverpool.com")
                .password(encoder.encode("Admin@L1V3rp0l!"))
                .roles(rolesAdmin)
                .status(Usuario.StatusUsuario.ACTIVO)
                .build());

        // Usuario operador
        Set<Usuario.Rol> rolesUser = new HashSet<>();
        rolesUser.add(Usuario.Rol.ROLE_USER);

        mongoTemplate.save(Usuario.builder()
                .nombre("Operador Logística")
                .email("operador@liverpool.com")
                .password(encoder.encode("Oper@L1V3rp0l!"))
                .roles(rolesUser)
                .status(Usuario.StatusUsuario.ACTIVO)
                .build());

        // Usuario solo lectura
        Set<Usuario.Rol> rolesViewer = new HashSet<>();
        rolesViewer.add(Usuario.Rol.ROLE_VIEWER);

        mongoTemplate.save(Usuario.builder()
                .nombre("Supervisor Ventas")
                .email("supervisor@liverpool.com")
                .password(encoder.encode("Super@L1V3rp0l!"))
                .roles(rolesViewer)
                .status(Usuario.StatusUsuario.ACTIVO)
                .build());

        log.info("3 usuarios de prueba insertados");
        log.info("Credenciales por defecto:");
        log.info("  ADMIN  -> admin@liverpool.com / Admin@L1V3rp0l!");
        log.info("  USER   -> operador@liverpool.com / Oper@L1V3rp0l!");
        log.info("  VIEWER -> supervisor@liverpool.com / Super@2L1V3rp0l024!");
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection(Usuario.class);
        log.warn("Rollback: colección 'usuarios' eliminada");
    }
}
