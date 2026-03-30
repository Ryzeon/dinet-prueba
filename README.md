# dinet-prueba

API en Java 17 y Spring Boot 3 para cargar pedidos desde un CSV, validarlos y guardarlos en Postgres. Arquitectura hexagonal, Flyway, JWT y OpenAPI.

---

## Cómo ejecutarlo

Hace falta JDK 17+, Docker y Maven (`./mvnw`).

Postgres con Docker (solo la base, para seguir usando `./mvnw spring-boot:run` contra `localhost`):

```bash
docker compose up -d postgres
```

Si quieres levantar también la API en contenedor (imagen multi-stage del `Dockerfile`):

```bash
docker compose up -d --build
```

La app en contenedor usa `POSTGRES_URL=jdbc:postgresql://postgres:5432/...` (el host `postgres` es el nombre del servicio en la red de Compose). En local sin Docker, el `application.yaml` sigue usando `localhost:5432`.

Por defecto: base `dinet_dev`, Postgres en el puerto 5432 del host, usuario y password `postgres`. La API en contenedor expone el 8080 del host (cambia con `APP_PORT` si lo necesitas). Alinea variables con `application.yaml` (`POSTGRES_DB`, etc.).

Arrancar la app:

```bash
./mvnw spring-boot:run
```

Flyway aplica las migraciones al subir; están en `src/main/resources/db/migration/`.

Tests:

```bash
./mvnw test
```

En tests uso H2 en memoria y no corro las migraciones de Flyway (el SQL está pensado para Postgres). Si rompes una migración SQL, los tests pueden seguir en verde: conviene probar también contra Docker.

Documentación interactiva: abre `http://localhost:8080/swagger-ui` (redirige a la UI). La spec JSON está en `/v3/api-docs`. Esas rutas van sin JWT; el endpoint de carga y el resto sí necesitan Bearer. El único otro público relevante es `/error` (Spring).

---

## Supuestos

- El CSV siempre trae cabecera en la línea 1. Los números de línea en errores son 1-based (la cabecera cuenta como línea 1).
- El archivo va en UTF-8.
- La fecha de entrega se compara con “hoy” en zona America/Lima (`Clock` en prod; en tests se puede fijar).
- Un cliente que exista en catálogo se acepta aunque esté inactivo en BD; si el negocio quisiera rechazar `activo = false`, habría que meterlo en dominio y tests.
- Idempotencia: misma `Idempotency-Key` + mismo contenido de archivo (hash) no vuelve a escribir pedidos. En replay el cuerpo del resumen va vacío; el cliente debe mirar la cabecera `X-Idempotent-Replayed`.

---

## Decisiones de diseño

- Postgres, tablas en snake_case, cambios de esquema solo con Flyway. Datos de ejemplo en V2 para no arrancar con catálogos vacíos.
- Código Java (dominio, puertos, nombres de clases) en inglés; nombres de tablas/columnas del enunciado en español.
- Paquetes: dominio y puertos bajo `me.ryzeon.dinet.orders`; adaptadores web y persistencia alineados a eso. El `@SpringBootApplication` escanea `me.ryzeon.dinet`.
- Formato alfanumérico del número de pedido se valida en dominio; en BD solo hay unicidad por `numero_pedido`, sin CHECK de formato.
- Catálogos: los puertos reciben conjuntos de ids y resuelven en una consulta por bloque, no una query por fila.
- JWT con HS256 y secreto en configuración (`dinet.security.jwt.secret`, mínimo 32 caracteres; en servidor se puede sobreescribir con `DINET_SECURITY_JWT_SECRET`). Resource server sin OIDC completo, suficiente para la prueba.
- Logs en JSON en perfil no-test + `X-Correlation-Id` en MDC; en test la consola queda legible con perfil `test`.

---

## Estrategia de batch

El enunciado pide procesar por lotes (500–1000). Aquí hay dos capas que van alineadas al mismo número (`dinet.orders.import.batch-size`):

1. En aplicación: el caso de uso acumula filas válidas hasta ese tamaño, valida y consulta catálogos por bloque, y persiste ese bloque.
2. En Hibernate: `hibernate.jdbc.batch_size` usa el mismo valor y `order_inserts` ayuda a agrupar INSERTs.

Valor por defecto 500: con ~1000 filas típicas son dos bloques; puedes subir a 1000 si quieres un solo bloque. Fuera de 500–1000 la app no arranca (validación de configuración). Se puede tunear por variable de entorno, por ejemplo `DINET_ORDERS_IMPORT_BATCH_SIZE`.

### Por qué no Spring Batch

No está en el `pom.xml` a propósito. Para este volumen (~mil filas por archivo y un POST por carga) no necesito un framework de jobs con tablas extra, steps y reanudación: con un caso de uso que lee el CSV en streaming, agrupa en memoria por bloque y persiste con JDBC batch alcanzo lo que pide el enunciado sin sumar otra pieza operativa. Si el día de mañana el volumen o las reglas fueran de ETL grande, ahí sí tendría sentido evaluar Spring Batch u otra herramienta.

### Duplicados y optimización con “hash” de strings

Para saber si un `numeroPedido` ya salió en el archivo o ya se guardó en esa misma importación, guardo esas claves en `HashSet<String>` (dos conjuntos: lo ya persistido en la ejecución y lo que va en el batch actual). Cada comprobación de “¿ya vi este número?” es O(1) amortizado respecto al tamaño del lote, porque el conjunto hash hace la búsqueda por clave en tiempo constante de media.

Antes, con un barrido lineal del batch por cada línea nueva, en el peor caso eras O(N) por fila dentro de un batch de tamaño N, o sea O(N²) acumulado en un bloque lleno. Pasar a conjuntos hash baja eso a O(1) por fila en esa parte, más barato cuando N va hacia 500–1000.

El SHA-256 del archivo entero es otra cosa: sirve para idempotencia HTTP junto con `Idempotency-Key`, no para duplicados de línea.

### Referencia de tiempos (una medición real en local)

Con un log de una carga con 5000 filas de datos (10 bloques de 500, `batch-size` 500), misma petición, Postgres en Docker en la misma máquina. Los `durationMs` son los que escribe `ImportOrdersService` por bloque (validación + catálogos + persistencia de ese lote):

| Bloque | ms (aprox.) |
|--------|-------------|
| 1 | 88 |
| 2–10 | 22–39 (la mayoría ~24–31) |

Sumando esos bloques salen unos 336 ms de trabajo por lotes. Se procesan alrededor de 13k filas por segundo respecto al tiempo total del HTTP. OJO El primer bloque suele ser el más lento (caches, conexión, plan); los siguientes se estabilizan.

No es un benchmark formal: cambia con hardware, índices, tamaño del CSV, red si el cliente no es local, etc. Sirve para ver que el batch JDBC + lotes en aplicación no se quedan pegados en segundos por cada 500 filas en un escenario razonable.

---

## Límites que conozco

- Tests con H2 no sustituyen validar SQL en Postgres.
- Tamaño máximo del multipart: 5MB por defecto (`application.yaml`).
- Dos requests paralelas con la misma clave y el mismo archivo pueden llegar a ejecutar la importación antes de que una marque idempotencia; la unicidad en BD evita duplicar filas de idempotencia y la de `numero_pedido` evita pedidos duplicados.
- Misma clave de idempotencia con archivos distintos (hash distinto) son operaciones distintas: conviven varias filas en `cargas_idempotencia`.

---

## API de carga

`POST /pedidos/cargar`, `multipart/form-data`, campo `file` (CSV). Cabeceras obligatorias: `Authorization: Bearer <JWT>` y `Idempotency-Key` no vacía. Opcional: `X-Correlation-Id`.

Respuesta 200: `OrderLoadSummary` con `totalProcesados`, `guardados`, `conError`, `erroresPorLinea` (cada ítem: `numeroLinea`, `codigo`, `detalle`; el detalle solo trae texto en casos puntuales como cabecera mala, duplicado en archivo o error leyendo el stream) y `erroresPorTipo` (`codigo`, `cantidad`).

Errores HTTP habituales: 401 sin JWT válido; 400 validación / cabeceras; 413 archivo grande; 404 ruta mala; 500 genérico. Cuerpo de error: `ApiErrorResponse` con `correlationId` alineado a `X-Correlation-Id`.

---

## Flujo rápido (una carga)

Filtros de correlación y acceso → controlador → servicio web (hash + idempotencia) → caso de uso (CSV línea a línea, validación dominio, bloques) → JPA con batch JDBC → resumen JSON y registro de idempotencia si tocaba.

En logs INFO verás por cada bloque algo como inicio/fin de import batch con `durationMs` (útil para ver tiempos reales).

---

## Tests, verify y JaCoCo

`./mvnw test` corre unitarios e integración ligera. `./mvnw verify` además ejecuta JaCoCo y falla si la cobertura de líneas del paquete `me.ryzeon.dinet.orders.domain` baja del 80%. Informe: `target/site/jacoco/index.html`. Lo puse para obligar a no dejar el dominio sin pruebas.

---

## Material

- Colección Postman (importar en Postman y ajustar variables `baseUrl`, `token`, `idempotencyKey`): [postman/dinet-prueba.postman_collection.json](postman/dinet-prueba.postman_collection.json).
- CSV de ejemplo: carpeta [samples/](samples/) e índice en [samples/INDEX.txt](samples/INDEX.txt).
- Enunciado / alcance del proyecto: [STATEMENT.MD](STATEMENT.MD).

---

## Migraciones

- V1: tablas base incluido `cargas_idempotencia`.
- V2: datos de ejemplo para local.
