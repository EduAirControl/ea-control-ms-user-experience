# AGENTS.md — ms-user-experience

Spring Boot 4.0.5 / Java 17 microservice, domain **user_experience**
(preferences and favorites). Package `com.eduaircontrol.userexperience`,
port **3006**, PostgreSQL schema **`user_experience`**
(DB `eduaircontrol_user_experience`). Governance:
`../docs-ea-control/09-microservices/services/07-ms-user-experience/`.

## Commands

```bash
./mvnw verify            # Testcontainers: Postgres (Docker needed)
./mvnw spring-boot:run   # needs PostgreSQL + POSTGRES_URL/USER/PASSWORD
docker compose up -d     # app :3006 + its own PostgreSQL on host :5436
```

`/health` is public; Swagger at `/swagger-ui.html`.

## Notes

- Domain modules at the root (`preferences`, `favorites`)
  plus `infrastructure` (`web`, `backfill`) and `shared`.
- Liquibase on startup (`src/main/resources/db/changelog/`); `ddl-auto=validate`.
  Tests run on **Testcontainers** (Postgres) — Docker Engine 29 needs
  `docker.api.version` (set for surefire in `pom.xml`).
- Auth validates **RS256** access tokens against the ms-security JWKS. Reads and
  writes: any valid token — every write is scoped to the token's own user via
  `currentUserId()` (no `ADMIN` role required, no cross-user writes).
- No notification API here: alerts are owned by ms-environment-monitoring
  (`GET /api/v1/alerts`). `user_experience.notification_state` is only a
  backfill staging table (copied by `UxBackfillService`).
- Use `spring-boot-starter-webmvc` (Spring Boot 4 name), not `-web`.
