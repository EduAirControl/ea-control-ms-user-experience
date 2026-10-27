-- Replica local ambiente -> institucion (ADR-016).
--
-- La alimenta el consumidor de classroom.educational_environment.* de
-- ms-classroom-management (ADR-007: consumidor idempotente via inbox_message).
-- Se usa para validar que un favorito o una valoracion referencia un ambiente de
-- la institucion del token.
CREATE TABLE user_experience.educational_environment (
    educational_environment_id UUID PRIMARY KEY,
    code                       VARCHAR(30),
    name                       VARCHAR(120),
    institution_id             UUID
);
