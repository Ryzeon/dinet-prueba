# dinet-prueba

Backend en Java 17 + Spring Boot 3. La idea es cargar pedidos desde un CSV, validarlos y guardarlos en Postgres. El repositorio sigue en desarrollo; abajo dejo anotado lo que falta para no perder el hilo.

## Cómo ejecutarlo en local

Necesitas JDK 17 (o superior), Docker y el wrapper de Maven (`./mvnw`).

Postgres:

```bash
docker compose up -d
```

Por defecto queda la base `dinet_dev` en el puerto 5432 con usuario y contraseña `postgres`. Si cambias la configuración, revisa `docker-compose.yml` y `application.yaml` (mismas variables, por ejemplo `POSTGRES_DB`).

Aplicación:

```bash
./mvnw spring-boot-run
```

Flyway se ejecuta al iniciar; los scripts están en `src/main/resources/db/migration/`.

Tests:

```bash
./mvnw test
```

En tests uso H2 en memoria y **no** aplico las migraciones de Flyway, porque el SQL está pensado para Postgres. Si rompes una migración, el test no lo va a detectar: conviene validar también con Docker.

Cuando el servicio esté arriba, SpringDoc publica la spec en `/v3/api-docs` y la UI en `/swagger-ui`.

## Qué está listo y qué falta

Por ahora hay Postgres con Docker, esquema Flyway (V1) y datos de prueba (V2: clientes como `CLI-123` / `CLI-999` y zonas `ZONA1`, `ZONA5`, etc.). También está definido el tamaño de lote en configuración (sección Batch), pero el código todavía no lo consume. Falta lo principal: endpoint de carga, reglas de negocio, JWT, el batch completo en código, tests de dominio, colección Postman y la carpeta `samples/`.

## Supuestos

Aún no dejo por escrito cosas como: si el CSV siempre trae cabecera, cómo numeramos la línea en los errores, encoding, límite de tamaño de archivo, si un cliente con `activo = false` entra o no en la validación. Cuando lo cierre en código, lo resumo aquí.

## Decisiones (por ahora)

Postgres con tablas en `snake_case` y cambios de esquema solo con Flyway. Las semillas van en la V2 para no partir de catálogos vacíos.

Lo que sigue (arquitectura hexagonal, idempotencia con hash, errores uniformes, JWT) lo voy sumando en esta sección conforme lo implemente.

## Batch

Clave en `application.yaml`: `dinet.pedidos.carga.batch-size`. Debe estar entre **500 y 1000**; fuera de ese rango el contexto de Spring no inicia.

**Por qué 500 por defecto:** el volumen esperado es del orden de **~1000 filas por archivo** (filas de datos del CSV, no cantidad de archivos). Con **500** divides ese peor caso en **dos bloques** (500 + 500): trabajas la carga en dos pasadas en lugar de enviar todo junto. Sigue cumpliendo el mínimo permitido (500) sin ir directo al máximo (1000).

Si configuras **1000**, en el peor caso tienes **un solo bloque** (todo el archivo en una pasada). También es válido; el número definitivo se puede afinar con métricas o pruebas cuando el import esté terminado.

Cada bloque más pequeño suele implicar menos filas por ronda de validación, por operaciones JDBC en batch y por transacción, lo que en general reduce el pico de memoria y el tiempo que la transacción permanece abierta, comparado con procesar 1000 filas de una sola vez.

Para sobrescribir sin tocar el YAML: variable de entorno `DINET_PEDIDOS_CARGA_BATCH_SIZE` (relaxed binding de Spring).

**Nota:** por ahora solo se valida al arranque; el flujo CSV/repositorios todavía no lee esta propiedad.

## Límites

En tests, H2 no es Postgres; eso ya quedó indicado arriba. El resto (cuántas filas conviene cargar, qué pasa si dos peticiones comparten la misma idempotency key, etc.) lo anoto cuando lo haya probado en serio.

## Migraciones

- **V1:** `clientes`, `zonas`, `pedidos`, `cargas_idempotencia`.
- **V2:** datos de ejemplo para desarrollo local.
