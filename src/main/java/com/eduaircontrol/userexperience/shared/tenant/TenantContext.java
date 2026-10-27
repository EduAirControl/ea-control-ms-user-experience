package com.eduaircontrol.userexperience.shared.tenant;

import java.util.UUID;

/**
 * Contexto de tenant de la petición (institución/sede) — ADR-016.
 *
 * <p>Lo puebla {@code JwtAuthFilter} al validar el token (claims
 * institutionId/campusId). Los servicios lo leen para validar que los ambientes
 * referenciados pertenecen a la institución del solicitante.
 *
 * <p>Sin petición o con un token sin institución, el contexto queda vacío y la
 * validación de pertenencia no se aplica.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> INSTITUTION = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CAMPUS = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID institutionId, UUID campusId) {
        INSTITUTION.set(institutionId);
        CAMPUS.set(campusId);
    }

    public static UUID institutionId() {
        return INSTITUTION.get();
    }

    public static UUID campusId() {
        return CAMPUS.get();
    }

    public static void clear() {
        INSTITUTION.remove();
        CAMPUS.remove();
    }
}
