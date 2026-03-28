# dinet-prueba

Backend en Java 17 + Spring Boot 3. La idea es cargar pedidos desde un CSV, validar y guardar en Postgres. El repo todavía está en construcción; lo que no esté hecho lo dejo anotado abajo para no olvidarme.

## Cómo levantarlo

Hace falta JDK 17 (o superior), Docker y el wrapper de Maven (`./mvnw`).

Postgres:

```bash
docker compose up -d
```

Por defecto levanta `dinet_dev` en el 5432 con user/pass `postgres`. Si cambias algo, el `docker-compose.yml` y `application.yaml` usan las mismas variables (`POSTGRES_DB`, etc.).

App:

```bash
./mvnw spring-boot-run
```

Flyway corre solo al iniciar; los scripts están en `src/main/resources/db/migration/`.

Tests:

```bash
./mvnw test
```

Ahí uso H2 en memoria y **no** corro las migraciones de Flyway, porque el SQL está escrito para Postgres. Si rompes algo en una migración, el test no te lo va a avisar: conviene probar con Docker.

Cuando el API esté prendido, SpringDoc deja la spec en `/v3/api-docs` y la UI en `/swagger-ui`.

## Qué hay hecho y qué no

Por ahora está armado el Postgres con Docker, el esquema con Flyway (V1) y unos datos de prueba (V2: clientes tipo `CLI-123` / `CLI-999` y zonas `ZONA1`, `ZONA5`, etc.). Falta todo lo demás: endpoint de carga, reglas de negocio en serio, seguridad JWT, batch, tests de dominio, Postman, carpeta `samples/`, lo que vaya saliendo.

## Supuestos

Todavía no fijé acá cosas como: si el CSV siempre trae cabecera, cómo contamos el número de línea en los errores, encoding, tope de tamaño de archivo, si un cliente `activo = false` cuenta o no para la validación. Cuando lo cierre en código, lo escribo en esta sección en una oración.

## Decisiones (por ahora)

Postgres con tablas en `snake_case` y cambios de esquema solo por Flyway. Las semillas van en la V2 para no arrancar con catálogos vacíos.

Lo que viene (hexagonal de verdad, idempotencia con hash, errores uniformes, JWT) lo voy sumando acá a medida que lo implemente, sin copy-paste de requisitos.

## Batch

Cuando esté la carga por lotes, explico en dos párrafos: tamaño de lote (configurable, del orden de cientos de filas), cómo leo el archivo sin meterlo entero en RAM y cómo hago los inserts. Hoy no hay nada que contar.

## Límites

H2 ≠ Postgres en los tests, ya lo dije. El resto (cuántas filas aguanta bien, qué pasa si dos requests pegan con la misma idempotency key, etc.) lo anoto cuando lo haya probado de verdad.

## Migraciones

- **V1:** `clientes`, `zonas`, `pedidos`, `cargas_idempotencia`.
- **V2:** datos demo para jugar en local.
