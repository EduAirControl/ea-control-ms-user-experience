package com.eduaircontrol.userexperience.shared.contract;

import java.util.Optional;
import java.util.UUID;

/**
 * Consulta la institución de un ambiente educativo a partir de la réplica local.
 *
 * <p>{@code ms-user-experience} no es dueño de los ambientes: los replica desde
 * los eventos de {@code ms-classroom-management} (ADR-007/016). Si la réplica aún
 * no ha recibido el ambiente, el resultado es vacío.
 */
public interface EnvironmentInstitutionPort {

    Optional<UUID> institutionOf(UUID environmentId);
}
