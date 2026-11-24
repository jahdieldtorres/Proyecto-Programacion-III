# Diccionario de datos de ORIGEN

Base de datos Oracle del proyecto ORIGEN (28 tablas, script `database/01_tablas.sql`).

**Autor:** Álvaro Aramendiz · Programación de Computadores III · Universidad Popular del Cesar

## Cómo leerlo

- **¿Nulo?**: `NO` significa que la columna es obligatoria (`NOT NULL`); `SÍ` significa que puede quedar vacía.
- **Restricción**: `PK` llave primaria, `FK → TABLA(COLUMNA)` llave foránea, `UNIQUE` valor que no se repite, `DEFAULT` valor por defecto, `CHECK` valores permitidos.
- **IDENTITY**: el ID lo genera Oracle solo; la app no lo envía en el `INSERT`.
- **Herencia (una tabla por subclase)**: la tabla padre guarda lo común y una columna de tipo; en cada subtabla la llave primaria es también llave foránea al padre (relación 1 a 1).
  - `COMPONENTS` → `TIRES`, `RIMS`, `ENGINES`, `TRANSMISSIONS` (un componente `GENERAL` no tiene subtabla)
  - `PARTNERS` → `SUPPLIERS`, `WORKSHOPS`
  - `VEHICLES` → `CARS`, `MOTORCYCLES`
- Estados en general: `ACTIVO` / `INACTIVO`. Eliminar es pasar a `INACTIVO` (borrado lógico).
- Valores de dinero: pesos colombianos, sin decimales.

## Índice por área

- **Catálogo base y usuarios:** [`BRANDS`](#brands), [`CATEGORIES`](#categories), [`USERS`](#users)
- **Ubicación:** [`DEPARTMENTS`](#departments), [`MUNICIPALITIES`](#municipalities), [`NEIGHBORHOODS`](#neighborhoods), [`USER_ADDRESSES`](#user_addresses)
- **Socios y servicios:** [`PARTNERS`](#partners), [`SUPPLIERS`](#suppliers), [`WORKSHOPS`](#workshops), [`SERVICES`](#services), [`FEES`](#fees), [`AFFILIATIONS`](#affiliations)
- **Repuestos:** [`COMPONENTS`](#components), [`TIRES`](#tires), [`RIMS`](#rims), [`ENGINES`](#engines), [`TRANSMISSIONS`](#transmissions), [`COMPONENT_IMAGES`](#component_images)
- **Vehículos y compatibilidad:** [`VEHICLES`](#vehicles), [`CARS`](#cars), [`MOTORCYCLES`](#motorcycles), [`COMPATIBILITY`](#compatibility)
- **Inventario y pedidos:** [`INVENTORY`](#inventory), [`INVENTORY_IMAGES`](#inventory_images), [`ORDERS`](#orders), [`ORDER_ITEMS`](#order_items), [`PRICE_HISTORY`](#price_history)

---

## Área: Catálogo base y usuarios

### BRANDS

Marcas de los repuestos (Bosch, Brembo...). No incluye marcas de vehículos.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la marca. |
| `NAME` | VARCHAR2(60) | NO | UNIQUE | Nombre de la marca. |

### CATEGORIES

Categorías para organizar el catálogo de repuestos.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la categoría. |
| `NAME` | VARCHAR2(60) | NO | UNIQUE | Nombre de la categoría. |
| `IMAGE_FILE` | VARCHAR2(120) | SÍ | — | Nombre del archivo de imagen de la categoría. |

### USERS

Personas que inician sesión en la app. Solo hay dos roles.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del usuario. |
| `NAME` | VARCHAR2(80) | NO | — | Nombre completo. |
| `EMAIL` | VARCHAR2(120) | NO | UNIQUE | Correo para iniciar sesión. |
| `PASSWORD_HASH` | VARCHAR2(64) | NO | — | Contraseña guardada como hash, nunca en texto plano. |
| `ROLE` | VARCHAR2(10) | NO | CHECK: ADMIN, CLIENTE | Rol del usuario en la app. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado del usuario. Eliminar es pasar a INACTIVO. |
| `REGISTRATION_DATE` | DATE | NO | DEFAULT SYSDATE | Fecha en que se registró. |

---

## Área: Ubicación

### DEPARTMENTS

Departamentos del país (primer nivel de ubicación).

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del departamento. |
| `NAME` | VARCHAR2(60) | NO | — | Nombre del departamento. |

### MUNICIPALITIES

Municipios de cada departamento.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del municipio. |
| `DEPARTMENT_ID` | NUMBER | NO | FK → DEPARTMENTS(ID) | Departamento al que pertenece. |
| `NAME` | VARCHAR2(60) | NO | — | Nombre del municipio. |

### NEIGHBORHOODS

Barrios de cada municipio.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del barrio. |
| `MUNICIPALITY_ID` | NUMBER | NO | FK → MUNICIPALITIES(ID) | Municipio al que pertenece. |
| `NAME` | VARCHAR2(60) | NO | — | Nombre del barrio. |

### USER_ADDRESSES

Direcciones de entrega de cada usuario.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la dirección. |
| `USER_ID` | NUMBER | NO | FK → USERS(ID) | Usuario dueño de la dirección. |
| `NEIGHBORHOOD_ID` | NUMBER | NO | FK → NEIGHBORHOODS(ID) | Barrio de la dirección. |
| `ADDRESS` | VARCHAR2(200) | NO | — | Dirección escrita (calle, número, etc.). |
| `IS_MAIN` | CHAR(1) | NO | DEFAULT 'N'; CHECK: S, N | S si es la dirección principal, N si no. |

---

## Área: Socios y servicios

### PARTNERS

Tabla padre de los socios. Guarda lo común de proveedores y talleres.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del socio. |
| `PARTNER_TYPE` | VARCHAR2(10) | NO | CHECK: PROVEEDOR, TALLER | Tipo de socio; define en qué subtabla está su fila. |
| `COMPANY_NAME` | VARCHAR2(120) | NO | — | Razón social o nombre del negocio. |
| `TAX_ID` | VARCHAR2(20) | NO | UNIQUE | NIT del socio. |
| `PHONE` | VARCHAR2(15) | NO | — | Teléfono de contacto. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado del socio. |
| `REGISTRATION_DATE` | DATE | NO | DEFAULT SYSDATE | Fecha en que se registró. |
| `ADDRESS` | VARCHAR2(200) | SÍ | — | Dirección del negocio. |
| `LATITUDE` | NUMBER(9,6) | SÍ | CHECK: LATITUDE entre -90 y 90 | Latitud para el mapa (-90 a 90). |
| `LONGITUDE` | NUMBER(9,6) | SÍ | CHECK: LONGITUDE entre -180 y 180 | Longitud para el mapa (-180 a 180). |

### SUPPLIERS

Subtabla de PARTNERS: los almacenes que venden repuestos. Relación 1 a 1 con su socio.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `PARTNER_ID` | NUMBER | NO | PK; FK → PARTNERS(ID) | Es el mismo ID del socio en PARTNERS. |
| `DELIVERY_DAYS` | NUMBER(3) | NO | CHECK: DELIVERY_DAYS >= 0 | Días que tarda en entregar. |

### WORKSHOPS

Subtabla de PARTNERS: los talleres. Relación 1 a 1 con su socio.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `PARTNER_ID` | NUMBER | NO | PK; FK → PARTNERS(ID) | Es el mismo ID del socio en PARTNERS. |
| `SPECIALTY` | VARCHAR2(80) | NO | — | Especialidad del taller. |
| `BAY_COUNT` | NUMBER(3) | NO | CHECK: BAY_COUNT >= 0 | Cantidad de puestos de trabajo. |

### SERVICES

Servicios que ofrece cada taller.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del servicio. |
| `WORKSHOP_ID` | NUMBER | NO | FK → WORKSHOPS(PARTNER_ID) | Taller que ofrece el servicio. |
| `NAME` | VARCHAR2(100) | NO | — | Nombre del servicio. |
| `DESCRIPTION` | VARCHAR2(300) | SÍ | — | Descripción corta del servicio. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado del servicio. |

### FEES

Tarifas (precios) de los servicios.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la tarifa. |
| `SERVICE_ID` | NUMBER | NO | FK → SERVICES(ID) | Servicio al que pertenece la tarifa. |
| `AMOUNT` | NUMBER(12) | NO | CHECK: AMOUNT >= 0 | Valor en pesos colombianos. |
| `START_DATE` | DATE | NO | — | Fecha desde la que rige la tarifa. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado de la tarifa. |

### AFFILIATIONS

Afiliación entre un proveedor y un taller.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la afiliación. |
| `SUPPLIER_ID` | NUMBER | NO | FK → SUPPLIERS(PARTNER_ID) | Proveedor afiliado. |
| `WORKSHOP_ID` | NUMBER | NO | FK → WORKSHOPS(PARTNER_ID) | Taller afiliado. |
| `START_DATE` | DATE | NO | — | Fecha de inicio de la afiliación. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado de la afiliación. |

---

## Área: Repuestos

### COMPONENTS

Tabla padre de los repuestos. Los repuestos no se borran, se pasan a INACTIVO.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del repuesto. |
| `COMPONENT_TYPE` | VARCHAR2(12) | NO | DEFAULT 'GENERAL'; CHECK: GENERAL, LLANTA, RIN, MOTOR, TRANSMISION | Tipo de repuesto; define en qué subtabla está su fila. GENERAL no tiene subtabla. |
| `NAME` | VARCHAR2(120) | NO | — | Nombre del repuesto. |
| `REFERENCE` | VARCHAR2(40) | NO | UNIQUE | Referencia o código del fabricante. |
| `BRAND_ID` | NUMBER | NO | FK → BRANDS(ID) | Marca del repuesto. |
| `CATEGORY_ID` | NUMBER | NO | FK → CATEGORIES(ID) | Categoría del repuesto. |
| `ORIGIN` | VARCHAR2(10) | NO | CHECK: ORIGINAL, GENERICO | Si el repuesto es original o genérico. |
| `DESCRIPTION` | VARCHAR2(300) | SÍ | — | Descripción corta. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado del repuesto. |

**Disparador `TRG_COMPONENTS_NO_DELETE`** (antes de borrar): no deja borrar repuestos (error -20003); se desactivan con `STATUS = 'INACTIVO'`.

### TIRES

Subtabla de COMPONENTS para llantas. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `COMPONENT_ID` | NUMBER | NO | PK; FK → COMPONENTS(ID) | Es el mismo ID del repuesto en COMPONENTS. |
| `WIDTH_MM` | NUMBER(4) | NO | — | Ancho en milímetros. |
| `ASPECT_RATIO` | NUMBER(3) | NO | — | Perfil de la llanta (alto como % del ancho). |
| `DIAMETER_INCHES` | NUMBER(3) | NO | — | Diámetro del rin en pulgadas. |
| `LOAD_INDEX` | NUMBER(3) | NO | — | Índice de carga. |
| `SPEED_INDEX` | VARCHAR2(2) | NO | — | Índice de velocidad (letra). |

### RIMS

Subtabla de COMPONENTS para rines. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `COMPONENT_ID` | NUMBER | NO | PK; FK → COMPONENTS(ID) | Es el mismo ID del repuesto en COMPONENTS. |
| `DIAMETER_INCHES` | NUMBER(4,1) | NO | — | Diámetro en pulgadas. |
| `WIDTH_INCHES` | NUMBER(4,1) | NO | — | Ancho en pulgadas. |
| `PCD` | VARCHAR2(20) | NO | — | Patrón de los tornillos (por ejemplo 5x114.3). |
| `MATERIAL` | VARCHAR2(10) | NO | CHECK: ALEACION, ACERO | Material del rin. |

### ENGINES

Subtabla de COMPONENTS para motores. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `COMPONENT_ID` | NUMBER | NO | PK; FK → COMPONENTS(ID) | Es el mismo ID del repuesto en COMPONENTS. |
| `DISPLACEMENT_CC` | NUMBER(5) | NO | — | Cilindraje en centímetros cúbicos. |
| `CYLINDERS` | NUMBER(2) | NO | — | Número de cilindros. |
| `FUEL` | VARCHAR2(10) | NO | CHECK: GASOLINA, DIESEL | Tipo de combustible. |
| `HORSEPOWER` | NUMBER(4) | NO | — | Potencia en caballos de fuerza. |

### TRANSMISSIONS

Subtabla de COMPONENTS para transmisiones. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `COMPONENT_ID` | NUMBER | NO | PK; FK → COMPONENTS(ID) | Es el mismo ID del repuesto en COMPONENTS. |
| `TRANSMISSION_TYPE` | VARCHAR2(12) | NO | CHECK: MANUAL, AUTOMATICA | Manual o automática. |
| `NUMBER_OF_SPEEDS` | NUMBER(2) | NO | — | Cantidad de velocidades. |

### COMPONENT_IMAGES

Fotos de los repuestos. En disco solo se guarda el nombre del archivo.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la imagen. |
| `COMPONENT_ID` | NUMBER | NO | FK → COMPONENTS(ID) | Repuesto al que pertenece la foto. |
| `FILE_NAME` | VARCHAR2(120) | NO | — | Nombre del archivo (carpeta images/components/). |
| `IS_MAIN` | CHAR(1) | NO | DEFAULT 'N'; CHECK: S, N | S si es la foto principal, N si no. |
| `SORT_ORDER` | NUMBER(3) | NO | DEFAULT 0 | Orden en que se muestra la foto. |

---

## Área: Vehículos y compatibilidad

### VEHICLES

Tabla padre de los vehículos.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del vehículo. |
| `VEHICLE_TYPE` | VARCHAR2(12) | NO | CHECK: COCHE, MOTOCICLETA | Tipo de vehículo; define en qué subtabla está su fila. |
| `BRAND_NAME` | VARCHAR2(60) | NO | — | Marca escrita como texto (BRANDS solo tiene marcas de repuestos). |
| `MODEL_NAME` | VARCHAR2(80) | NO | — | Modelo del vehículo. |
| `YEAR_RELEASE` | NUMBER(4) | NO | CHECK: YEAR_RELEASE entre 1900 y 2100 | Año del modelo. |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado del vehículo. |

### CARS

Subtabla de VEHICLES para coches. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `VEHICLE_ID` | NUMBER | NO | PK; FK → VEHICLES(ID) | Es el mismo ID del vehículo en VEHICLES. |
| `NUM_DOORS` | NUMBER(2) | NO | — | Número de puertas. |
| `CAR_TYPE` | VARCHAR2(12) | NO | CHECK: CAMIONETA, HATCHBACK, SEDAN, DEPORTIVO | Tipo de carrocería. |

### MOTORCYCLES

Subtabla de VEHICLES para motocicletas. Relación 1 a 1.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `VEHICLE_ID` | NUMBER | NO | PK; FK → VEHICLES(ID) | Es el mismo ID del vehículo en VEHICLES. |
| `DISPLACEMENT_CC` | NUMBER(5) | NO | — | Cilindraje en centímetros cúbicos. |
| `MOTORCYCLE_TYPE` | VARCHAR2(12) | NO | CHECK: SCOOTER, NAKED, DEPORTIVAS | Tipo de motocicleta. |

### COMPATIBILITY

Qué repuestos sirven para qué vehículos (relación muchos a muchos).

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `COMPONENT_ID` | NUMBER | NO | PK (compuesta); FK → COMPONENTS(ID) | Repuesto compatible. |
| `VEHICLE_ID` | NUMBER | NO | PK (compuesta); FK → VEHICLES(ID) | Vehículo con el que es compatible. |

La llave primaria es compuesta: `COMPONENT_ID, VEHICLE_ID`.

---

## Área: Inventario y pedidos

### INVENTORY

Oferta de un proveedor por un repuesto: precio, stock y condiciones. No hay UNIQUE (proveedor, repuesto); InventoryService evita dos ofertas ACTIVAS repetidas.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la oferta. |
| `SUPPLIER_ID` | NUMBER | NO | FK → SUPPLIERS(PARTNER_ID) | Proveedor que hace la oferta. |
| `COMPONENT_ID` | NUMBER | NO | FK → COMPONENTS(ID) | Repuesto ofrecido. |
| `PRICE` | NUMBER(12) | NO | CHECK: PRICE > 0 | Precio unitario en pesos colombianos. |
| `STOCK` | NUMBER(6) | NO | DEFAULT 0; CHECK: STOCK >= 0 | Unidades disponibles. |
| `SHIPPING_COST` | NUMBER(10) | NO | DEFAULT 0; CHECK: SHIPPING_COST >= 0 | Costo de envío en pesos. |
| `WARRANTY_MONTHS` | NUMBER(3) | NO | DEFAULT 0; CHECK: WARRANTY_MONTHS >= 0 | Garantía en meses. |
| `CONDITION` | VARCHAR2(15) | NO | DEFAULT 'NUEVO'; CHECK: NUEVO, USADO, REACONDICIONADO | Estado físico del repuesto. |
| `DESCRIPTION` | VARCHAR2(500) | SÍ | — | Descripción que escribe la tienda sobre su oferta (estado real, qué incluye). |
| `STATUS` | VARCHAR2(10) | NO | DEFAULT 'ACTIVO'; CHECK: ACTIVO, INACTIVO | Estado de la oferta. |

**Disparador `TRG_INVENTORY_PRICE`** (después de cambiar `PRICE`): si el precio cambió, guarda una fila en `PRICE_HISTORY` con el precio anterior y el nuevo.

### INVENTORY_IMAGES

Fotos de cada oferta (de la tienda). COMPONENT_IMAGES es la foto general del producto; esta es la de cada tienda.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la foto. |
| `INVENTORY_ID` | NUMBER | NO | FK → INVENTORY(ID) | Oferta a la que pertenece la foto. |
| `FILE_NAME` | VARCHAR2(120) | NO | — | Nombre del archivo (carpeta images/inventory/). |
| `CAPTION` | VARCHAR2(120) | SÍ | — | Texto corto que explica qué se ve (por ejemplo, "Incluye llave de rin"). |
| `IS_MAIN` | CHAR(1) | NO | DEFAULT 'N'; CHECK: S, N | S si es la foto principal de la oferta, N si no. |
| `SORT_ORDER` | NUMBER(3) | NO | DEFAULT 0 | Orden en que se muestra la foto. |

### ORDERS

Pedidos de los clientes (el carrito ya confirmado).

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del pedido. |
| `USER_ID` | NUMBER | NO | FK → USERS(ID) | Cliente que hizo el pedido. |
| `ORDER_DATE` | DATE | NO | DEFAULT SYSDATE | Fecha y hora del pedido. |
| `STATUS` | VARCHAR2(12) | NO | DEFAULT 'PENDIENTE'; CHECK: PENDIENTE, CONFIRMADO, CANCELADO | Estado del pedido. |
| `TOTAL` | NUMBER(14) | NO | DEFAULT 0; CHECK: TOTAL >= 0 | Total del pedido en pesos. |

### ORDER_ITEMS

Líneas de cada pedido: qué oferta y cuántas unidades.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador de la línea. |
| `ORDER_ID` | NUMBER | NO | FK → ORDERS(ID) | Pedido al que pertenece. |
| `INVENTORY_ID` | NUMBER | NO | FK → INVENTORY(ID) | Oferta comprada. |
| `QUANTITY` | NUMBER(4) | NO | CHECK: QUANTITY > 0 | Unidades pedidas. |
| `UNIT_PRICE` | NUMBER(12) | NO | CHECK: UNIT_PRICE > 0 | Precio unitario al momento de comprar. |
| `SHIPPING_COST` | NUMBER(10) | NO | DEFAULT 0; CHECK: SHIPPING_COST >= 0 | Costo de envío de la línea. |

**Disparador `TRG_ORDER_ITEMS_STOCK`** (antes de insertar): revisa que haya stock en `INVENTORY` y lo descuenta. Si no alcanza, lanza el error -20001. Java no resta el stock.

### PRICE_HISTORY

Historial de cambios de precio de las ofertas. Lo llena un disparador.

| Columna | Tipo | ¿Nulo? | Restricción | Descripción |
|---|---|---|---|---|
| `ID` | NUMBER | NO | PK; IDENTITY | Identificador del registro. |
| `INVENTORY_ID` | NUMBER | NO | FK → INVENTORY(ID) | Oferta cuyo precio cambió. |
| `OLD_PRICE` | NUMBER(12) | NO | — | Precio anterior. |
| `NEW_PRICE` | NUMBER(12) | NO | — | Precio nuevo. |
| `CHANGE_DATE` | DATE | NO | DEFAULT SYSDATE | Fecha del cambio (la pone la base). |

Esta tabla la llena el disparador `TRG_INVENTORY_PRICE`; la app no inserta aquí.

---

## Disparadores (resumen)

| Disparador | Tabla | Cuándo | Qué hace | Error |
|---|---|---|---|---|
| `TRG_ORDER_ITEMS_STOCK` | `ORDER_ITEMS` | Antes de insertar | Revisa y descuenta el stock de `INVENTORY` | -20001 sin stock; -20002 la oferta no existe |
| `TRG_INVENTORY_PRICE` | `INVENTORY` | Después de cambiar `PRICE` | Guarda el cambio en `PRICE_HISTORY` | — |
| `TRG_COMPONENTS_NO_DELETE` | `COMPONENTS` | Antes de borrar | Impide borrar repuestos | -20003 |

## Índices

Oracle no crea índices en las llaves foráneas. Estos se crearon en las más consultadas: `IX_COMPONENTS_BRAND`, `IX_COMPONENTS_CATEGORY`, `IX_COMPONENT_IMAGES_COMP`, `IX_COMPATIBILITY_VEHICLE`, `IX_INVENTORY_IMAGES_INV`, `IX_INVENTORY_COMPONENT`, `IX_SERVICES_WORKSHOP`, `IX_FEES_SERVICE`, `IX_AFFILIATIONS_SUPPLIER`, `IX_AFFILIATIONS_WORKSHOP`, `IX_USER_ADDRESSES_USER`, `IX_ORDERS_USER`, `IX_ORDER_ITEMS_ORDER`, `IX_ORDER_ITEMS_INVENTORY`, `IX_PRICE_HISTORY_INV`.
