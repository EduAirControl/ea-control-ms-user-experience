package com.eduaircontrol.userexperience.infrastructure.messaging.classroom;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato del ciclo de vida de ambientes educativos publicado por
 * ms-classroom-management.
 *
 * <p>Copia local del contrato del productor (ADR-016: contrato duplicado a
 * proposito, sin dependencia entre servicios). Los tres eventos comparten sobre y
 * solo se distinguen por {@code eventType} y la routing key.
 */
public record ClassroomEnvironmentEvent(
        UUID eventId,
        String eventType,
        UUID aggregateId,
        String aggregateType,
        Instant occurredAt,
        Integer version,
        Payload payload) {

    public static final String CREATED_TYPE = "EducationalEnvironmentCreated";

    public static final String UPDATED_TYPE = "EducationalEnvironmentUpdated";

    public static final String REMOVED_TYPE = "EducationalEnvironmentRemoved";

    public static final int SCHEMA_VERSION = 1;

    public record Payload(
            UUID environmentId,
            String code,
            String name,
            UUID campusId,
            UUID institutionId,
            UUID environmentTypeId,
            Integer floor,
            String status) {
    }

    public ClassroomEnvironmentEvent {
        if (version == null || version <= 0) {
            version = SCHEMA_VERSION;
        }
    }

    /** Nombre del consumidor en inbox_message. */
    public static final String CONSUMER = "classroom-environment-replica";
}
