# Assistant Backend

## Prerequisites

- JDK 25
- Maven 3.9.16
- MySQL 8.0

## Required environment variables

Set `DB_PASSWORD` to the password of the local `special_ed_app` MySQL user. Optional variables are `DB_URL`, `DB_USERNAME`, `SPRING_PROFILES_ACTIVE`, and `SERVER_PORT`.

## Verify

Run `mvn test`, then `mvn spring-boot:run`. The health endpoint is `GET http://localhost:8080/health`.

Before the first database-backed run, execute `sql/schema.sql` and `sql/seed.sql` using an existing authorized MySQL connection, then create the local `special_ed_app` account and set `DB_PASSWORD` outside Git.

## Member B API

- `POST /behavior` creates a structured ABC behavior record.
- `GET /behavior/{studentId}` lists a student's records.
- `GET /statistics/{studentId}` groups the student's records by behavior type.

Import `docs/openapi.yaml` into Apifox. The seed script creates demo user `1`, demo student `1`, and the initial A/B/C dictionaries. Replace or extend these dictionaries after the frontend confirms the final option list.
