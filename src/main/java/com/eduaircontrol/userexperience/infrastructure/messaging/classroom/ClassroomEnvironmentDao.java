package com.eduaircontrol.userexperience.infrastructure.messaging.classroom;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO de la réplica local de ambientes ({@code user_experience.educational_environment}).
 *
 * <p>Solo se guarda lo necesario para el aislamiento por institución: el id y la
 * institución del ambiente. La retirada es un borrado físico: una fila marcada
 * seguiría resolviéndose para favoritos nuevos.
 */
@Repository
@RequiredArgsConstructor
public class ClassroomEnvironmentDao {

    private final JdbcTemplate jdbc;

    @Transactional
    public void upsert(UUID environmentId, String code, String name, UUID institutionId) {
        jdbc.update("""
                INSERT INTO user_experience.educational_environment
                    (educational_environment_id, code, name, institution_id)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (educational_environment_id) DO UPDATE SET
                    code = EXCLUDED.code,
                    name = EXCLUDED.name,
                    institution_id = EXCLUDED.institution_id
                """, environmentId, code, name, institutionId);
    }

    @Transactional
    public void markRemoved(UUID environmentId) {
        jdbc.update(
                "DELETE FROM user_experience.educational_environment "
                        + "WHERE educational_environment_id = ?",
                environmentId);
    }
}
