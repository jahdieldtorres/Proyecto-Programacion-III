# Tabla GRASP y SOLID · ORIGEN

Principios y patrones que usamos en el proyecto, con una clase real de `develop`
donde se ve cada uno. Autor: Héctor Guevara.

Cómo leerla: la columna **Dónde lo usamos** dice la clase y lo que hace; para la
exposición basta con decir esa frase y el "por qué ayuda".

| # | Principio | Qué significa | Dónde lo usamos | Por qué ayuda |
|---|---|---|---|---|
| 1 | **Experto** (GRASP) | La responsabilidad la tiene la clase que ya tiene los datos para cumplirla. | `Cart` calcula el subtotal, el envío y el total porque es la que tiene las líneas. `Order.getTotal()` suma sus propias líneas. | El cálculo vive en un solo lugar y el total nunca queda distinto de las líneas. |
| 2 | **Creador** (GRASP) | Quien contiene o usa muy de cerca a otro objeto es quien lo crea. | `Cart.toOrder(userId)` crea el `Order` y sus `OrderItem`, porque el carrito tiene los datos de cada línea. | Nadie más necesita saber cómo se arma un pedido. |
| 3 | **Controlador** (GRASP) | Una clase recibe los eventos de la pantalla y los pasa a la lógica, sin hacer el trabajo ella misma. | `CatalogController` recibe la búsqueda y los clics y llama a `AppServices.components()`. No tiene SQL ni reglas. | Se puede cambiar la pantalla sin tocar las reglas, y al revés. |
| 4 | **Bajo acoplamiento** (GRASP) | Las clases dependen lo mínimo unas de otras. | `OrderService` solo conoce la interfaz `OrderDAO`, no sabe si abajo hay Oracle o archivos. | Si cambia la base de datos, el servicio no se toca. |
| 5 | **Alta cohesión** (GRASP) | Cada clase hace cosas que están relacionadas entre sí. | `PasswordHasher` solo convierte contraseñas en hash. `FeeService` solo tiene reglas de tarifas. | Las clases son cortas, fáciles de explicar y de probar. |
| 6 | **Polimorfismo** (GRASP) | Distintos tipos responden al mismo mensaje cada uno a su manera. | `Partner` es abstracta y `Supplier` y `Workshop` implementan `toLine()` cada uno con sus datos. `ComponentDAO` se usa igual con `ComponentFileDAO` o `ComponentOracleDAO`. | No hace falta un `if` por cada tipo: se llama al método y cada clase sabe qué hacer. |
| 7 | **S**: Responsabilidad única (SOLID) | Una clase tiene un solo motivo para cambiar. | `OrderOracleDAO` solo guarda y lee pedidos. `OrderService` solo valida y arma el pedido. | Si cambia el SQL, se toca el DAO; si cambia una regla, se toca el servicio. |
| 8 | **O**: Abierto/cerrado (SOLID) | Se puede agregar comportamiento nuevo sin modificar lo que ya funciona. | Pasamos de archivos a Oracle agregando `ComponentOracleDAO`; `ComponentService` no cambió. | Menos riesgo de dañar lo que ya estaba probado. |
| 9 | **L**: Sustitución de Liskov (SOLID) | Un hijo puede usarse donde se espera al padre sin que nada falle. | `ComponentOracleDAO` devuelve `Tire`, `Rim`, `Engine` o `Transmission` y `ComponentService` y `CatalogController` los tratan como `Component`. | El código que usa al padre funciona con todos los tipos. |
| 10 | **I**: Segregación de interfaces (SOLID) | Es mejor varias interfaces pequeñas que una gigante. | Cada tabla tiene su DAO: `BrandDAO`, `FeeDAO`, `OrderDAO`... y `FeeService` solo recibe `FeeDAO`. | Cada clase depende solo de los métodos que usa. |
| 11 | **D**: Inversión de dependencias (SOLID) | Las clases importantes dependen de interfaces, no de clases concretas. | `OrderService` recibe un `OrderDAO` por el constructor, y `AppServices` es quien le pone `OrderOracleDAO`. | Se puede cambiar o probar con otro DAO sin tocar el servicio. |
| 12 | **Patrón DAO** | Una clase aparte se encarga de hablar con la base de datos. | `ComponentDAO` es la interfaz y `ComponentOracleDAO` la implementa con JDBC; `OrderOracleDAO` hace lo mismo con los pedidos. | El SQL queda en un solo lugar y el resto del programa no sabe nada de la base. |
| 13 | **Patrón Singleton** (versión simple) | Hay una sola instancia compartida de algo. | `AppServices` crea una sola vez cada servicio (`static final`) y todas las pantallas usan esos mismos. `Session` guarda un único usuario y un único carrito (`static`). | Todas las pantallas comparten el mismo usuario, carrito y servicios. |

## Notas para la exposición

- **Singleton:** lo usamos en su versión simple, con campos `static`. No es el
  Singleton clásico con constructor privado y `getInstance()`; si preguntan,
  esa es la diferencia.
- **Controlador:** en el proyecto lo hacen los controladores de JavaFX
  (`CatalogController`, `ComparatorController`...). `OrderService.checkout`
  hace de controlador del caso de uso "confirmar pedido" para la lógica.
- **Polimorfismo con DAO:** es el mismo mecanismo que la inversión de
  dependencias (fila 11): el servicio llama a la interfaz y la implementación
  se elige en `AppServices`.
