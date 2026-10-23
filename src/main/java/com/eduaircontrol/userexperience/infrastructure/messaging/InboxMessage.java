package com.eduaircontrol.userexperience.infrastructure.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marca de un mensaje ya procesado, para idempotencia del consumidor (ADR-007).
 *
 * <p>La garantia del productor es at-least-once, asi que el mismo evento puede
 * llegar mas de una vez. Se escribe en la <b>misma transaccion</b> que la replica,
 * de modo que nunca queda registrado un mensaje cuya proyeccion no se guardó.
 */
@Entity
@Table(name = "inbox_message", schema = "user_experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxMessage {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Column(nullable = false, length = 60)
    private String consumer;

    @Column(name = "message_type", nullable = false, length = 80)
    private String messageType;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
