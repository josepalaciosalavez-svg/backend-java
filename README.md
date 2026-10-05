# Sistema de Gestión de Pedidos y Entregas — Liverpool

Proyecto fullstack para la administración de clientes, lugares de entrega y pedidos, compuesto por una API REST en Spring Boot, base de datos MongoDB y un frontend en React + TypeScript.

---

## Requisitos previos

- **Docker** y **Docker Compose**
- **Java 8** (o superior) y **Maven 3.8+**
- **Node.js 18+** y **npm**

---

## 1. Base de datos con Docker

El proyecto incluye un `docker-compose.yml` en la raíz con MongoDB 6 y un panel web de administración (Mongo Express).

Para iniciar los contenedores:

```bash
docker compose up -d
```

Servicios disponibles:
- **MongoDB:** `localhost:27017` (Base de datos: `liverpool_db`)
- **Mongo Express:** [http://localhost:8081](http://localhost:8081)

Para apagar los contenedores:
```bash
docker compose down
```

Si necesitas reiniciar la base de datos limpia desde cero:
```bash
docker compose down -v
docker compose up -d
```

---

## 2. Backend (Spring Boot)

El backend corre sobre Spring Boot 2.7 y utiliza **Mongock** para aplicar índices y sembrar datos de prueba automáticamente en la primera ejecución.

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

*Nota:* Si tienes Maven instalado mediante Homebrew en macOS y no está en tu PATH global, puedes ejecutar `/opt/homebrew/bin/mvn spring-boot:run`.

El servicio quedará disponible en:
- **API Base:** `http://localhost:8080/api`
- **Documentación Swagger UI:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **OpenAPI JSON:** `http://localhost:8080/api/api-docs`

---

## 3. Frontend (React + Vite)

El frontend está desarrollado con React 19, Material-UI y Vite.

```bash
cd frontend
npm install
npm run dev
```

La aplicación web estará disponible en:
- **Portal Web:** [http://localhost:3000](http://localhost:3000)

---

## Credenciales de prueba

Al iniciar el backend, Mongock inserta automáticamente usuarios con contraseñas encriptadas en BCrypt:

| Perfil | Email | Contraseña | Rol | Alcance |
|---|---|---|---|---|
| **Administrador** | `admin@liverpool.com` | `Admin@2024!` | `ROLE_ADMIN` | Control total del sistema (CRUD completo) |
| **Operador** | `operador@liverpool.com` | `Oper@2024!` | `ROLE_USER` | Gestión operativa de clientes, entregas y pedidos |
| **Supervisor** | `supervisor@liverpool.com` | `Super@2024!` | `ROLE_VIEWER` | Solo lectura |
| **Cliente** | `carlos.ramirez@correo.com` | `Cliente@2024!` | `ROLE_CLIENTE` | Acceso a sus propios pedidos y lugares de entrega |
| **Cliente** | `maria.gonzalez@correo.com` | `Cliente@2024!` | `ROLE_CLIENTE` | Acceso a sus propios pedidos y lugares de entrega |

---

## Modelo de datos y reglas de negocio

1. **Entregas (`/api/entregas`):**
   - Representan los domicilios o puntos de entrega registrados para los clientes.
   - Manejan únicamente estatus: `ACTIVO` o `INACTIVO`.

2. **Pedidos (`/api/pedidos`):**
   - Representan las órdenes de compra asociadas a un cliente y a un lugar de entrega.
   - Manejan el flujo logístico del paquete: `PENDIENTE`, `PROCESANDO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

3. **Acceso de clientes:**
   - Los clientes pueden iniciar sesión con su correo y contraseña.
   - El backend filtra automáticamente las consultas por su identificador de cliente (`clienteId`), asegurando que solo puedan visualizar y gestionar su propia información.
   - El acceso a la administración de usuarios del sistema (`/api/usuarios`) está restringido a administradores.

---

## Variables de entorno

El backend cuenta con valores por defecto listos para desarrollo local en `backend/src/main/resources/application.yml` o vía `backend/.env`:

```properties
MONGO_URI=mongodb://localhost:27017/liverpool_db
JWT_SECRET=4c6f6e672d616e642d7365637572652d6b65792d666f722d6c6976657270006f
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200,http://localhost:5173
```
