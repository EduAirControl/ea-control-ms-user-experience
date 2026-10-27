# ms-user-experience

Microservicio de **preferencias y favoritos del usuario** (puerto `3006`,
esquema PostgreSQL `user_experience`).

Dueño de las preferencias y favoritos. No posee usuarios, ambientes ni variables:
los referencia por UUID. Las APIs de ratings e historial de búsqueda fueron retiradas;
sus filas históricas se conservan con nombres `legacy_*`.

## Contrato

`07-api/contracts/openapi/` (repositorio de documentacion, por definir).

## Arranque local

Requires PostgreSQL on `localhost:5432` (database `eduaircontrol_user_experience`).
Authentication is RS256 via the ms-security JWKS (ADR-006): there is no local secret.
Environment variables live in `.env.example` (copy it to `.env` and adjust the values).

```bash
# local (necesita PostgreSQL con la BD eduaircontrol_user_experience)
POSTGRES_USER=user_experience_user POSTGRES_PASSWORD=user_experience_pass ./mvnw spring-boot:run

# o stack del servicio (app + PostgreSQL)
docker compose up --build
```

## Verificacion

```bash
curl http://localhost:3006/health
```
