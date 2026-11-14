# Base de datos de ORIGEN (Oracle)

Scripts para Oracle Autonomous Database. Se corren en **Database Actions → SQL**
con el botón *Ejecutar script* (F5), en este orden:

| Orden | Archivo | Qué hace | Quién |
|---|---|---|---|
| 1 | `01_tablas.sql` | Borra las tablas de ORIGEN (si existen) y las crea de nuevo. | Álvaro (A3) |
| 2 | `02_datos.sql` | Carga los datos de prueba (los mismos de `data/*.csv`) y muestra un conteo de revisión. | Álvaro (A3, A6) |
| 3 | `03_disparadores.sql` | Descuenta stock, guarda historial de precios y bloquea el borrado de componentes. | Héctor (H5) |

Se pueden repetir 1 y 2 las veces que haga falta: los ID se reinician al recrear las tablas.
Usar siempre el usuario `ORIGEN`, no `ADMIN`.

## Herencia: una tabla por subclase

Así queda definida la herencia para todo el proyecto (DAO de Oracle y MER). La tabla padre guarda lo común y una columna de tipo. Cada subtabla usa como llave
primaria la llave foránea a su padre (relación 1 a 1).

| Padre | Columna de tipo | Subtablas |
|---|---|---|
| `COMPONENTS` | `COMPONENT_TYPE` (`GENERAL`, `LLANTA`, `RIN`, `MOTOR`, `TRANSMISION`) | `TIRES`, `RIMS`, `ENGINES`, `TRANSMISSIONS` |
| `PARTNERS` | `PARTNER_TYPE` (`PROVEEDOR`, `TALLER`) | `SUPPLIERS`, `WORKSHOPS` |
| `VEHICLES` | `VEHICLE_TYPE` (`COCHE`, `MOTOCICLETA`) | `CARS`, `MOTORCYCLES` |

Un componente `GENERAL` no tiene fila en ninguna subtabla. Los textos guardados son los
mismos de los `.csv`, así los DAO no traducen valores.

**Qué implica para los DAO (A4 y H4):**
- Guardar un componente, socio o vehículo con subtipo son **dos `INSERT` en una transacción**
  (`setAutoCommit(false)`, `commit()`, y `rollback()` si falla). Al insertar el padre, pedir el ID
  generado con `prepareStatement(sql, new String[]{"ID"})`.
- Leer es un `LEFT JOIN` del padre con sus subtablas, según el tipo.
- Las llaves foráneas a un proveedor o taller apuntan a `SUPPLIERS(PARTNER_ID)` y
  `WORKSHOPS(PARTNER_ID)`, que es el mismo número que `PARTNERS.ID`.

## Cómo se llaman las cosas respecto al código Java

| Java / `.csv` | Base de datos |
|---|---|
| `Component.origin` (`ORIGINAL`/`GENERICO`) | `COMPONENTS.ORIGIN` |
| `Tire.profile` | `TIRES.ASPECT_RATIO` (perfil; `PROFILE` es palabra de Oracle) |
| `Car.numDoors`, `Car.type` | `CARS.NUM_DOORS`, `CARS.CAR_TYPE` |
| `Motorcycle.cylinders` (cilindraje en cc) | `MOTORCYCLES.DISPLACEMENT_CC` |
| `Vehicle.brand` | `VEHICLES.BRAND_NAME` (texto: `BRANDS` solo tiene marcas de repuestos) |
| `Transmission.transmissionType` | `TRANSMISSIONS.TRANSMISSION_TYPE` (`MANUAL` / `AUTOMATICA`) |
| `UserAddress.principal` (`SI`/`NO`) | `USER_ADDRESSES.IS_MAIN` (`'S'`/`'N'`) |
| fechas `AAAA-MM-DD` | columnas `DATE` (`rs.getDate(...).toLocalDate()`) |

## Reglas que ya cuida la base

Estados, roles, condición, tipos y materiales con `CHECK`; precio mayor que 0; stock,
envío y garantía no negativos; correo, NIT y referencia únicos; latitud y longitud dentro
de rango. Las validaciones normales siguen en los servicios: la base es una protección extra.

`INVENTORY` no tiene restricción de unicidad por proveedor y componente: una oferta
desactivada sigue en la tabla y se puede crear otra igual. Que no haya dos ofertas
**activas** repetidas lo cuida `InventoryService`.

## Datos pendientes de otros puntos

- **Dirección y coordenadas de los socios:** provisionales. Cuando entre H3, usar las mismas de `data/partners.csv`.
- **Imágenes y descripciones** de componentes (`COMPONENT_IMAGES`, `COMPONENTS.DESCRIPTION`) y
  `CATEGORIES.IMAGE_FILE`: se llenan en A5, A6 y A8.
- `ORDERS`, `ORDER_ITEMS` y `PRICE_HISTORY` empiezan vacías; las llena la aplicación y los disparadores.

## Agregar datos nuevos

Los ID los genera la base en orden de inserción y las llaves foráneas de `02_datos.sql` usan
esos números. Agreguen filas **al final del bloque de su tabla** y después de las tablas de
las que dependen; no reordenen las que ya están.
