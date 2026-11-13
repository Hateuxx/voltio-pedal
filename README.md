# Voltio & Pedal — Sistema de control de inventario

Sistema integral para la gestión de inventario, proveedores y usuarios. El backend expone una API REST con autenticación JWT, control de acceso por roles y protección contra fuerza bruta, diseñada para ser consumida por el cliente web.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 25, Spring Boot 4.1.2 |
| Seguridad | Spring Security, OAuth2 Resource Server (JWT HS256 con Nimbus) |
| Documentación API| Swagger (springdoc-openapi) |
| Base de Datos | MySQL, Flyway (Migraciones) |
| Frontend | React + Vite (en desarrollo) |

## Requisitos
- **Java Development Kit (JDK) 25:** Es obligatorio usar la versión 25 exacta (versiones más nuevas pueden ser incompatibles con la versión actual de Lombok).
- **Base de Datos:** MySQL (probado con la versión 26.7.0).
- **Control de Versiones:** Git.

*Nota: No es necesario tener Maven instalado en tu máquina, ya que el proyecto incluye su propio wrapper (`mvnw` / `mvnw.cmd`).*

## Base de datos

**⚠️ IMPORTANTE:** 
1. **No crees las tablas a mano:** Flyway se encargará de crear y estructurar las tablas automáticamente al arrancar la aplicación (mediante la migración `V1`).
2. **Cambia la contraseña:** En el siguiente script, reemplaza `'tu_contrasena_segura'` por una clave real.

Abre tu cliente SQL (como **MySQL Workbench** o **DBeaver**) como administrador y ejecuta el siguiente script para crear la base de datos y un usuario dedicado:

```sql
-- 1. Crear la base de datos
CREATE DATABASE voltio_pedal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Crear el usuario dedicado 
CREATE USER 'voltio_user'@'localhost' IDENTIFIED BY 'tu_contrasena_segura';

-- 3. Otorgar permisos SOLO sobre la base de datos del sistema
GRANT ALL PRIVILEGES ON voltio_pedal.* TO 'voltio_user'@'localhost';
```

## Variables de entorno

El sistema utiliza las siguientes variables de entorno para su configuración. Si una variable tiene un valor por defecto en el código, no es estrictamente obligatoria para arrancar el entorno de desarrollo, pero se recomienda definirlas todas por seguridad.

| Variable | Para qué sirve | Obligatoria | Ejemplo |
|---|---|---|---|
| `DB_USERNAME` | Usuario dedicado de la base de datos (no usar `root`) | Sí | `voltio_user` |
| `DB_PASSWORD` | Contraseña del usuario de la BD | Sí | `tu_contrasena_segura` |
| `JWT_SECRET` | Clave secreta en Base64 (256 bits mínimo) para firmar los tokens | Sí | *(ver Generar JWT_SECRET)* |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial (DNI `70000001`). Debe cumplir la política de contraseñas | Sí, en el primer arranque (si ya existe un admin, se ignora) | `CambiaEsto#2026` |
| `ADMIN_RESPUESTA` | Respuesta a la pregunta de seguridad del admin inicial (*"¿Nombre de la tienda?"*) | Sí, en el primer arranque | *(el nombre que elijas)* |
| `CORS_ORIGENES` | Orígenes (URLs) permitidos para comunicarse con la API | No (por defecto `http://localhost:5173`) | `http://localhost:5173` |

## Generar JWT_SECRET

La clave secreta para firmar los tokens debe ser de **mínimo 32 bytes (256 bits)**. Esta es la longitud mínima estricta que exige el algoritmo HMAC-SHA256 (HS256) validado por nuestra clase `JwtConfig`. Si generas una más larga, funcionará perfectamente.

Para generar una clave segura en formato Base64, abre tu terminal y ejecuta el comando correspondiente a tu sistema. Copia el resultado para usarlo en el siguiente paso.

**En Windows (PowerShell):**
```powershell
$bytes = New-Object Byte[] 32; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)
```

## Arrancar el sistema

Crea tu script de arranque dentro de la carpeta `backend/apiinventario/` (junto a los archivos mvnw y pom.xml).

**⚠️ Este script contiene tus contraseñas reales.** El repositorio ya lo ignora
(`.gitignore`). Verifica con `git status` que **no aparezca** antes de cada commit.

### Windows (PowerShell)
Crea un archivo llamado `iniciar.ps1` en `backend/apiinventario/` con el siguiente contenido, reemplazando los valores de ejemplo por los reales:

```powershell
# iniciar.ps1 — NO subir a Git
$env:DB_USERNAME     = 'voltio_user'
$env:DB_PASSWORD     = 'tu_contrasena_segura'
$env:JWT_SECRET      = '(pega_aqui_tu_clave_base64_generada)'
$env:ADMIN_PASSWORD  = 'CambiaEsto#2026'
$env:ADMIN_RESPUESTA = '(el nombre que elijas)'
$env:CORS_ORIGENES   = 'http://localhost:5173'

.\mvnw.cmd spring-boot:run
```

Para arrancar el proyecto, abre PowerShell y ejecuta: `.\iniciar.ps1`

## Probar la API

Una vez que la aplicación esté corriendo, sigue estos pasos para comprobar su funcionamiento y hacer tu primera petición autenticada:

### Comprobar que el sistema está vivo (Health Check)
Abre tu navegador, Postman o cURL y accede a:
👉 **http://localhost:8080/actuator/health**

Si el backend arrancó correctamente, recibirás esta respuesta:
```json
{"groups":["liveness","readiness"],"status":"UP"}
```

### Abrir la documentación interactiva (Swagger)
La interfaz interactiva con todos los endpoints de la API está disponible en:
👉 **http://localhost:8080/swagger-ui/index.html**

### Iniciar sesión como administrador
Para consumir los endpoints protegidos, primero debes autenticarte.
1. Despliega el endpoint **`POST /api/auth/login`** y haz clic en **"Try it out"**.
2. En el *Request body*, envía el siguiente JSON (asegúrate de usar la contraseña que colocaste en tu script de arranque):
   ```json
   {
     "tipoDocumento": "DNI",
     "numeroDocumento": "70000001",
     "contrasena": "(tu ADMIN_PASSWORD)"
   }
   ```
3. Haz clic en **Execute**.
4. En la sección "Server response", copia el valor del token devuelto (solo la cadena de texto larga, **sin las comillas**).

### Autorizar Swagger para el resto de la API
1. Sube al inicio de la página de Swagger y haz clic en el botón verde **Authorize**.
2. En el cuadro de texto, pega tu token.
   - ⚠️ **Importante:** Pega **SOLO** el token. **NO** escribas la palabra "Bearer" y **NO** incluyas comillas ni espacios en blanco. Swagger ya añade el prefijo automáticamente, por lo que si lo escribes a mano recibirás un error `401 Unauthorized` en todas tus peticiones.
3. Haz clic en **Authorize** y cierra la ventana. ¡Ya tienes vía libre para probar el resto del sistema!

### Expiración del token
Por motivos de seguridad, el token tiene un tiempo de vida de **1 hora**. 

Si después de un rato de inactividad tus peticiones empiezan a fallar devolviendo errores `401 Unauthorized`, no es un bug del código: tu token simplemente ha expirado. Vuelve a Iniciar sesión como administrador, inicia sesión de nuevo para generar un nuevo token y actualízalo en el botón *Authorize*.

### ¿Todas las peticiones devuelven 401?
- Pegaste el token **con comillas**.
- Escribiste **`Bearer `** delante del token.
- El token **expiró** (dura 1 hora).
- **Cambiaste `JWT_SECRET`** y reiniciaste: los tokens anteriores dejan de ser válidos.

## Roles y permisos

| Recurso | Administrador | Operador de tienda | Técnico |
|---|---|---|---|
| Usuarios (crear, listar, desactivar, reactivar) | ✅ | ❌ | ❌ |
| Proveedores — consultar | ✅ | ✅ | ❌ |
| Proveedores — registrar, modificar, eliminar, reactivar | ✅ | ❌ | ❌ |
| Proveedores inactivos — consultar | ✅ | ❌ | ❌ |
| Clientes — consultar | ✅ | ✅ | ✅ |
| Clientes — registrar | ✅ | ✅ | ❌ |
| Tipos de repuesto — consultar | ✅ | ✅ | ✅ |
| Tipos de repuesto — registrar, modificar, eliminar | ✅ | ❌ | ❌ |
| Ingresos de mercadería — consultar | ✅ | ✅ | ❌ |
| Ingresos de mercadería — registrar *(próximamente)* | ✅ | ✅ | ❌ |
| Cambiar mi propia contraseña | ✅ | ✅ | ✅ |
| Recuperar contraseña (sin sesión) | Público | Público | Público |

> Sin token → **401**. Con token, pero sin el rol necesario → **403**.

## Problemas frecuentes

### `Access denied for user ...`
* **Síntoma:** Error de acceso denegado reportado por la base de datos al intentar arrancar la aplicación.
* **Causa:** La contraseña configurada en la variable `DB_PASSWORD` es incorrecta, el usuario de MySQL/MariaDB no existe, o no cuenta con los permisos (`GRANT`) necesarios sobre la base de datos. *(Nota: Si la variable de entorno no estuviera definida en absoluto, el error previo sería de tipo "Could not resolve placeholder").*
* **Solución:** Revisa tu script de arranque (`iniciar.ps1` o `iniciar.sh`), asegúrate de que el usuario y la contraseña coincidan exactamente con los creados en MySQL (`CREATE USER`), y valida que tenga privilegios sobre el esquema.

### `Communications link failure`
* **Síntoma:** El backend no logra establecer conexión con la base de datos al iniciar.
* **Causa:** El servicio de MySQL o MariaDB está detenido, o la base de datos está configurada en un puerto distinto al 3306.
* **Solución:** Verifica que el servicio de la base de datos esté activo. En Windows, puedes comprobarlo abriendo PowerShell y ejecutando:
  ```powershell
  Get-Service MySQL*
  ```

### `Could not resolve placeholder 'DB_USERNAME'`
* **Síntoma:** Spring Boot falla al arrancar indicando que no encuentra las propiedades de conexión.
* **Causa:** Intentaste ejecutar `./mvnw` directamente o desde tu IDE sin inyectar las variables de entorno requeridas por el `application.yaml`.
* **Solución:** Utiliza siempre el script de arranque correspondiente (`.\iniciar.ps1` o `./iniciar.sh`) dentro de la carpeta `backend/apiinventario/` para que cargue automáticamente las credenciales.

### `JWT_SECRET inválido o muy corto`
* **Síntoma:** Error al iniciar la aplicación relacionado con la longitud de la clave de seguridad del token.
* **Causa:** La clave secreta configurada tiene menos de 32 bytes (256 bits), incumpliendo la validación estricta que exige el algoritmo HMAC-SHA256 en `JwtConfig`.
* **Solución:** Genera una clave segura de mínimo 32 bytes utilizando los comandos Base64 provistos en la sección Generar JWT_SECRET de este README.

### `...la ejecución de scripts está deshabilitada en este sistema`
* **Síntoma:** Al intentar ejecutar `.\iniciar.ps1` en PowerShell, Windows bloquea la ejecución.
* **Causa:** La política de seguridad por defecto de Windows prohíbe la ejecución de scripts locales `.ps1`.
* **Solución:** Abre PowerShell de forma normal (**sin necesidad de privilegios de Administrador**) y ejecuta la siguiente orden:
  ```powershell
  Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
  ```

> `RemoteSigned` permite ejecutar scripts **locales** (como tu `iniciar.ps1`), pero exige firma a los **descargados** de internet: no desactiva toda la seguridad de tu equipo.

### Errores de compilación o de Lombok
* **Síntoma:** Fallos extraños de compilación, anotaciones de Lombok no reconocidas o errores al empaquetar la aplicación.
* **Causa:** Incompatibilidad entre el SDK de Java instalado en tu equipo (una versión distinta de la 25) y el código del proyecto.
* **Solución:** Verifica tu versión ejecutando `java -version` en la terminal y asegúrate de tener configurado y activo estrictamente el **JDK 25**.

### Un cambio en el código "no se aplica"
* **Síntoma:** Modificas el código fuente, ejecutas la aplicación y el comportamiento sigue siendo exactamente el anterior (o Maven arroja *"Nothing to compile"*).
* **Causa:** Maven está utilizando archivos de clase (`.class`) previamente compilados y cacheados en la carpeta `target/` (falsos positivos de compilación incremental).
* **Solución:** Ejecuta un comando con limpieza previa para destruir la caché y obligar a compilar desde cero:
  ```powershell
  .\mvnw.cmd clean spring-boot:run
  ```

### Cambié `ADMIN_PASSWORD` y no puedo iniciar sesión
* **Síntoma:** Actualizas la variable `ADMIN_PASSWORD` en tu script de arranque, inicias sesión en Swagger con el DNI del administrador inicial y obtienes error `401 Unauthorized`.
* **Causa:** El componente `InicializadorAdmin` solo crea al administrador si **no existe ninguno**. Si el usuario ya existe en la base de datos, ignora por completo el cambio en `ADMIN_PASSWORD`.
* **Solución:** 
  1. Utilizar el flujo de recuperación de contraseña (RF3) a través de la pregunta secreta (`/api/auth/recuperar/pregunta` y `/api/auth/recuperar/restablecer`).
  2. **Último recurso (solo en desarrollo):** reemplazar el hash de la contraseña directamente en la tabla `usuario`, o eliminar y volver a crear la base de datos (⚠️ **esto borra todos los datos**). Al arrancar, Flyway recreará las tablas y `InicializadorAdmin` creará de nuevo al administrador con tu `ADMIN_PASSWORD`.

## Flujo de Trabajo y Contribución

Para mantener la estabilidad y limpieza de la API, todo el equipo sigue un flujo de trabajo estricto respaldado tanto por disciplina como por automatización técnica:

### Gestión de ramas y protección de `main`
Nadie hace commits directos a la rama `main`. Para evitar depender exclusivamente de la memoria o disciplina del equipo (del mismo modo que usamos `.gitignore` en lugar de confiar en no subir archivos locales), **protegemos formalmente la rama en GitHub**:
* La regla está en **`Settings` → `Rules` → `Rulesets`** (*"Regla para main"*), aplicada a la rama por defecto, con:
* **Require a pull request before merging** (0 aprobaciones obligatorias).
* **Block force pushes**: nadie puede reescribir el historial de `main`.
* **Restrict deletions**: nadie puede borrar `main`.
* De esta manera, aunque alguien lo intente por despiste, GitHub rechazará automáticamente cualquier intento de hacer un `git push` directo a `main`.
* Toda tarea se desarrolla obligatoriamente en ramas independientes utilizando prefijos limpios:
  * `feature/` para nuevas funcionalidades (ej. `feature/modulo-clientes`, `feature/gestion-usuarios`).
  * `fix/` para corrección de errores (ej. `fix/error-401-token`).
  * `docs/` para documentación (ej. `docs/actualizar-readme`).

### Mensajes de commit (Conventional Commits)
Los commits deben seguir estrictamente el estándar de Conventional Commits, utilizando alcances entre paréntesis (`scope`) para mantener un historial quirúrgico y legible. Ejemplos reales del historial del proyecto:
* `feat(auth): recuperar contrasena con pregunta de seguridad (RF3)`
* `fix(config): exigir DB_USERNAME sin valor por defecto`
* `refactor(auth): extraer validarSecreto para reutilizar el bloqueo por intentos`

### Verificación previa al commit
Antes de cada `git commit`, sigue este orden:
```powershell
git status
git add <archivos>
git diff --staged
git commit -m "tipo(alcance): descripción"
```
> Si `git diff --staged` sale **vacío**, **no hagas el commit**: probablemente el archivo no está guardado (●) o editaste otro archivo.

### Pull Requests (PR) y revisión
Para integrar código a `main`, se debe abrir un Pull Request que cumpla con los siguientes requisitos:
* **Título:** Debe seguir el formato de Conventional Commit con su respectivo alcance (ej. `feat(clientes): agregar endpoints de gestión de clientes`).
* **Evidencia:** Incluir una tabla de pruebas que demuestre que los endpoints fueron validados con éxito (por ejemplo, respuestas de Swagger o evidencias de la base de datos).
* **Revisión:** Se recomienda la revisión de al menos un compañero (*peer review*) antes del merge.
* **Merge:** La integración final se realiza mediante un **Merge Commit** para preservar la trazabilidad histórica exacta en el repositorio.

### Tolerancia cero con secretos y credenciales
**Nunca subas secretos al repositorio.** Está estrictamente prohibido commitear contraseñas, tokens, claves de base de datos o el valor de `JWT_SECRET`. 
* *Precaución adicional en evidencias:* Revisa cuidadosamente tus capturas de pantalla (consola, Swagger o base de datos) antes de subirlas al repositorio o adjuntarlas en un PR, asegurándote de que no se filtren tokens de sesión activos, claves privadas o datos reales de producción.

### Regla de oro del equipo
> **Todo código o texto copiado de otra herramienta (IA, tutoriales, Stack Overflow) se verifica contra el proyecto antes de usarlo: que las clases, métodos, librerías y tablas existan de verdad aquí. Si no puedes explicar una línea, no la subas.**