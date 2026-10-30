# ORIGEN · Portal de Repuestos

Proyecto de aula de **Programación III** (Universidad Popular del Cesar).
Aplicación de escritorio en Java que reúne en un solo lugar lo que ofrecen distintos proveedores de repuestos: el usuario busca un componente y compara, lado a lado, el precio, el envío, el stock, la garantía y el tiempo de entrega de cada proveedor.

| Integrante | Rol |
|---|---|
| Jahdiel Torres | Líder técnico |
| Álvaro Aramendiz | Desarrollador |
| Héctor Guevara | Desarrollador |

## Tecnologías

- Java 21 o superior, proyecto Maven en NetBeans
- JavaFX 21 (vistas FXML diseñadas en Scene Builder, estilos en CSS)
- Persistencia en archivos planos (carpeta `data/`). La base de datos Oracle (modelo v5) queda pendiente para cuando se vea en clase.

## Cómo ejecutar

- **NetBeans:** abrir la carpeta del proyecto y dar **Run** (el archivo `nbactions.xml` hace que use `javafx:run`).
- **Consola:** `mvn clean javafx:run`

La primera vez Maven descarga JavaFX, así que necesita internet.

## Estructura

```
src/main/java/autoparts/
├── model/         Entidades del dominio
├── dao/           Interfaces DAO
│   └── file/      Implementaciones con archivos planos
├── service/       Reglas de negocio y validaciones
├── view/          Controladores JavaFX y Navigator
├── exception/     Excepciones propias
├── util/          Rutas de archivos y utilidades de lectura/escritura
└── Main.java      Arranca la aplicación

src/main/resources/autoparts/view/   Archivos .fxml y styles.css
data/                                Archivos de datos (.csv)
docs/                                Documentación del proyecto
```

**Regla de capas:** la vista solo llama a servicios; los servicios solo llaman a interfaces DAO; nadie lee ni escribe archivos fuera de `dao.file`. Los servicios reciben el DAO por constructor.

### Crear una pantalla (plantilla)

Todas las pantallas de listado parten de la misma plantilla, para que se vean iguales:
`ListTemplate.fxml` + `ListTemplateController` (título, buscador, filtro, tabla y mensajes).

1. Copiar `src/main/resources/autoparts/view/ListTemplate.fxml` con el nombre de la pantalla. Las del menú ya están conectadas por nombre en `DashboardController`: `Catalog.fxml`, `Comparator.fxml`, `Partners.fxml` y `MyAccount.fxml` (mientras no existan, se muestra "Sección en construcción").
2. Copiar `ListTemplateController.java` como `NombreController` y cambiar en el `.fxml` el `fx:controller`.
3. Seguir los comentarios `REEMPLAZAR:`: la clase del modelo en la tabla, las columnas, las opciones del filtro y la llamada al servicio.
4. Si el servicio es nuevo, agregarlo en `AppServices` con su FileDAO, por ejemplo:
   `private static final ComponentService COMPONENT_SERVICE = new ComponentService(new ComponentFileDAO(), ...);` y su método `components()`.
5. Borrar lo marcado como "Solo para la plantilla" (`ExampleItem`, `exampleData`, `filter`).

**Reglas de diseño**
- Partir siempre de la plantilla; no empezar una pantalla de cero.
- Usar solo clases de `styles.css` (`page-title`, `primary-button`, `secondary-text`, `highlight-row`...). Nada de `style="..."` en el `.fxml` ni colores escritos a mano.
- Si hace falta un estilo nuevo, va al final de `styles.css`, en una sección con el nombre del módulo.
- El controlador solo llama a servicios (vía `AppServices`); nunca a un DAO ni a `FileUtil`.
- Los errores se muestran en `lblMessage` (capturando `InvalidDataException` y `PersistenceException`), no en ventanas emergentes.

Si usan IA para hacer su pantalla, el prompt está en `docs/prompt-pantallas.md`.

## Convenciones

**Idioma:** el código (paquetes, clases, métodos, atributos, `fx:id`, clases CSS y nombres de archivos de datos) va en **inglés**. Los textos que ve el usuario, los comentarios Javadoc, la documentación y la descripción de los commits van en **español**.

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas | `autoparts.service` |
| Clases del modelo | singular, PascalCase | `Component`, `Supplier` |
| Interfaces DAO / implementaciones | Entidad + `DAO` / Entidad + `File` + `DAO` (luego `Oracle`) | `ComponentDAO`, `ComponentFileDAO` |
| Servicios | Entidad + `Service` | `ComparatorService` |
| Vistas / controladores | `Name.fxml` + `NameController` | `Login.fxml`, `LoginController` |
| fx:id de los componentes | prefijo de tipo + nombre, camelCase | `txtEmail`, `btnLogin`, `tblOffers` |
| Métodos y atributos | camelCase, verbos para métodos | `findByComponent()`, `unitPrice` |
| Constantes | MAYÚSCULAS con guion bajo | `MY_ACCOUNT` |
| Archivos de datos | plural, minúsculas, `.csv` | `components.csv` |

Cada clase lleva un comentario Javadoc (`/** ... */`) que explica qué hace y su `@author`. Sin comentarios innecesarios: solo donde algo no se entiende a simple vista.

**Formato de los archivos de datos:** una línea por registro, campos separados por `|`, UTF-8, sin encabezado. En las clases con herencia, el primer campo es el tipo (`SUPPLIER`, `WORKSHOP`, `TIRE`...). Los campos siguen el orden de las columnas del modelo v5; la correspondencia entre atributo y columna se documenta en `docs/`.

## Trabajo con Git

- Ramas: `main` (solo entregas) ← `develop` (integración) ← `feature/*` (un módulo por rama).
- Nadie hace commit directo a `main` ni a `develop`: todo entra por **Pull Request a `develop`**, aprobado por otro integrante, y se fusiona con **merge commit** (no squash).
- Mensajes de commit: `tipo(módulo): descripción`, con tipos `feat`, `fix`, `docs`, `refactor`. Ejemplo: `feat(catalog): agregar DAO de componente`.
- Antes de abrir un PR: actualizar la rama con `develop` y resolver los conflictos en la rama propia.
- Los datos de prueba de `data/` se suben, pero no los cambios que quedan al probar (`git restore data/` antes del commit).

```bash
git checkout develop && git pull
git checkout -b feature/mi-modulo
# ... commits ...
git push -u origin feature/mi-modulo
```
