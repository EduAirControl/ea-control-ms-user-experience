package com.eduaircontrol.userexperience.infrastructure.persistence;

import com.eduaircontrol.userexperience.shared.contract.EnvironmentInstitutionPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Institucion de un ambiente contra la replica local {@code educational_environment}.
 */
@Repository
@RequiredArgsConstructor
public class EnvironmentInstitutionAdapter implements EnvironmentInstitutionPort {

    private final JdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> institutionOf(UUID environmentId) {
        if (environmentId == null) {
            return Optional.empty();
        }
        return jdbc.query("""
                        select institution_id
                        from user_experience.educational_environment
                        where educational_environment_id = ?
                        """,
                        (rs, rowNum) -> rs.getObject("institution_id", UUID.class),
                        environmentId)
                .stream().findFirst();
    }
}
