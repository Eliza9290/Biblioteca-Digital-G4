

API REST con Spring Boot 4 y Java 25 para gestionar los préstamos de libros de una biblioteca. Combina los préstamos guardados en H2 con los datos reales de cada libro (título oficial, páginas, editorial y portada) obtenidos por su ISBN de [Open Library](https://openlibrary.org/developers/api): un mashup con caché RAM.

## Cómo arrancarla

1. Abrir el proyecto en IntelliJ IDEA (JDK 25 de Corretto).
2. Ejecutar `BibliotecaDigitalG4Application`. Con el perfil `dev` se cargan 4 préstamos de prueba.
3. Swagger UI: http://localhost:8080/swagger-ui.html
4. Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bibliotecadb`, usuario `sa`, sin contraseña).
5. Peticiones de ejemplo (éxito, fallback y error): `pruebas.http`.

## Perfiles

| Perfil | Base de datos | Consola H2 | ddl-auto |
| --- | --- | --- | --- |
| `dev` (por defecto) | H2 en memoria | activada | `update` |
| `test` | H2 en memoria aislada | desactivada | `create-drop` |
| `prod` | H2 en fichero `./data/` | desactivada | `validate` |

## Regla de negocio

Cada socio puede tener como máximo **3 préstamos** a la vez. Si se supera, la API responde **422**.

## Endpoints

| Método | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/api/prestamos` (`?titulo=`, `?email=`) | 200 |
| GET | `/api/prestamos/{id}` | 200 / 404 |
| GET | `/api/prestamos/{id}/libro` | 200 / 404 (mashup con caché) |
| POST | `/api/prestamos` | 201 / 400 / 409 / 422 |
| PUT | `/api/prestamos/{id}` | 200 / 400 / 404 / 409 / 422 |
| PATCH | `/api/prestamos/{id}` | 200 / 400 / 404 / 422 |
| DELETE | `/api/prestamos/{id}` | 204 / 404 |

## Tests

Clic derecho en `src/test/java` → **Run 'All Tests'**, o `./mvnw test`. Son 25 tests: integración, unitario de controlador, unitario de servicio y de repositorio.