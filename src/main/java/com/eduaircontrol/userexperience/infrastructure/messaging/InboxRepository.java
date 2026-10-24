package com.eduaircontrol.userexperience.infrastructure.messaging;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InboxRepository extends JpaRepository<InboxMessage, UUID> {

    /**
     * Registra el mensaje solo si no estaba visto antes y dice si lo registro.
     *
     * <p>{@code ON CONFLICT DO NOTHING} en lugar de capturar la excepcion de clave
     * primaria: en PostgreSQL un error aborta la transaccion entera.
     */
    @Modifying
    @Query(value = """
            INSERT INTO user_experience.inbox_message
                (message_id, consumer, message_type, received_at)
            VALUES (:messageId, :consumer, :messageType, now())
            ON CONFLICT (message_id) DO NOTHING
            """, nativeQuery = true)
    int claim(@Param("messageId") UUID messageId,
              @Param("consumer") String consumer,
              @Param("messageType") String messageType);
}
