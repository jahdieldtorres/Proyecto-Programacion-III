# Formatos de los archivos de datos

Este documento define los campos y su orden en cada archivo de `data/`. Es la
referencia de todos los DAO de ` dao.file` y de la lista de verificación del
Pull Request. Si cambia un formato, se actualiza aquí en el mismo PR.

## Reglas generales

- Una línea por registro. Campos separados por `|`. Codificación UTF-8. Sin encabezado.
- Los saltos de línea que escribe `FileUtil` son siempre `\n`.
- Los valores no pueden contener `|` ni saltos de línea (el servicio lo valida).
- Un campo vacío se escribe sin nada: `a||c`. Los campos vacíos al final se conservan.
- Las líneas vacías y las que empiezan por `#` se ignoran al leer. **No** se
  ponen comentarios dentro de los archivos: al reescribir (actualizar o
  eliminar) se perderían. Los formatos se documentan solo aquí.
- Los identificadores son números enteros. El siguiente identificador se
  obtiene con `FileUtil.nextId(ruta, columnaDelId)`.
- Las fechas se guardan como `AAAA-MM-DD`. Los valores monetarios, en pesos
  colombianos, sin separador de miles ni símbolo (`185000`).
- Los estados se guardan en mayúsculas. Casi todos los registros usan
  `ACTIVO` / `INACTIVO`; eliminar un registro es cambiar su estado a
  `INACTIVO` (borrado lógico), salvo que el módulo indique otra cosa.
- En los archivos con herencia, el **primer campo es el tipo** y el
  identificador es el **segundo** (`nextId(ruta, 1)`). En los demás, el
  identificador es el primero (`nextId(ruta, 0)`).

## Cómo usar las utilidades

| Clase | Qué hace |
|---|---|
| `util.FlatFiles` | Constantes con las rutas (`FlatFiles.INVENTORY`, `FlatFiles.USERS`...). |
| `util.FileUtil.read(ruta)` | Líneas de datos. Devuelve lista vacía si el archivo no existe. |
| `util.FileUtil.readFields(ruta)` | Igual, pero cada línea ya separada en `String[]`. |
| `util.FileUtil.append(ruta, linea)` | Guardar: agrega una línea al final. |
| `util.FileUtil.rewrite(ruta, lineas)` | Actualizar o eliminar: se reescribe todo el archivo. |
| `util.FileUtil.join(campos...)` | Para el `toLine()` de las entidades. |
| `util.FileUtil.split(linea)` | Para el constructor "desde línea" de las entidades. |
| `util.FileUtil.nextId(ruta, col)` | Siguiente identificador libre. |
| `exception.PersistenceException` | Error de lectura o escritura (no verificada). |

Ejemplo de `toLine()` y constructor desde línea:

```java
public String toLine() {
    return FileUtil.join("PROVEEDOR", id, companyName, nit, phone, status,
            registrationDate, deliveryDays);
}

public Supplier(String line) {
    String[] f = FileUtil.split(line);
    this.id = Integer.parseInt(f[1]);
    this.companyName = f[2];
    // ...
}
```

## Datos de prueba

- **Contraseña de todos los usuarios de prueba:** `Test1234*`
- Se guarda como hash **SHA-256 en hexadecimal minúsculas**
  (`7546df0d5385f6a05d11c5a0388a5736466f31d5ab210fe486f377bd648459a5`).
  `UserService` debe calcular el mismo hash al validar el login.
  Si el equipo elige otro algoritmo, hay que regenerar `users.csv`.
- Usuarios: `admin@example.com` (ADMIN), `carlos.perez@example.com` y
  `maria.gomez@example.com` (CLIENTE), `proveedor@example.com` (SOCIO del
  proveedor 1), `taller@example.com` (SOCIO del taller 3) e
  `inactivo@example.com` (CLIENTE inactivo, para probar que no puede entrar).
- Casos para probar el comparador: el componente 5 lo ofrecen 4 proveedores
  (uno sin stock); el 8 y el 10 tienen precios y envíos distintos; el 4 tiene
  una oferta `INACTIVO`; el componente 15 está `INACTIVO` y no debe aparecer
  en la búsqueda.

---

## Ubicación

### `departments.csv`
`id|nombre`

```
1|Cesar
```

### `municipalities.csv`
`id|id_departamento|nombre`

```
1|1|Valledupar
```

### `neighborhoods.csv`
`id|id_municipio|nombre`

```
1|1|Los Músicos
```

## Usuarios

### `users.csv`
`id|nombre|correo|clave_hash|rol|estado|fecha_registro|id_socio`

- `rol`: `CLIENTE`, `ADMIN` o `SOCIO`.
- `id_socio`: identificador en `partners.csv` si el rol es `SOCIO`; `0` en los demás.
- El correo no se puede repetir.

```
2|Carlos Pérez|carlos.perez@example.com|7546df0d...|CLIENTE|ACTIVO|2026-10-26|0
```

### `user_addresses.csv`
`id|id_usuario|id_barrio|direccion|principal`

- `principal`: `SI` o `NO`. Un usuario tiene como máximo una dirección principal.

```
1|2|1|Calle 15 # 12-34|SI
```

## Socios

### `partners.csv` (herencia: Socio → Proveedor / Taller)
Campos comunes: `tipo|id|nombre_compania|nit|telefono|estado|fecha_registro`

| Tipo | Campos propios (después de los comunes) |
|---|---|
| `PROVEEDOR` | `tiempo_entrega_dias` |
| `TALLER` | `especialidad|numero_bahias` **(\*)** |

```
PROVEEDOR|1|AutoMax Repuestos|900123456-1|3001112233|ACTIVO|2026-10-26|2
TALLER|3|Taller El Rápido|900555444-7|3004445566|ACTIVO|2026-10-26|Frenos|4
```

### `services.csv` (servicios que ofrece un taller)
`id|id_taller|nombre|descripcion|estado`

```
1|3|Cambio de pastillas de freno|Reemplazo de pastillas delanteras o traseras|ACTIVO
```

### `fees.csv`
`id|id_servicio|valor|fecha_inicio|estado` **(\*)**

- Un servicio puede tener varias tarifas; la vigente es la `ACTIVO`.

```
1|1|60000|2026-10-26|ACTIVO
```

### `affiliations.csv` (proveedor afiliado a un taller)
`id|id_proveedor|id_taller|fecha_inicio|estado` **(\*)**

```
1|1|3|2026-10-26|ACTIVO
```

## Catálogo

### `brands.csv`
`id|nombre`

```
4|Michelin
```

### `categories.csv`
`id|nombre`

```
1|Frenos
```

### `components.csv` (herencia: Componente → Llanta / Rin / Motor / Transmisión)
Campos comunes: `tipo|id|nombre|referencia|id_marca|id_categoria|calidad|estado`

- `calidad`: `ORIGINAL` o `GENERICO`.
- `referencia` no se puede repetir.

| Tipo | Campos propios (después de los comunes) | Valores |
|---|---|---|
| `GENERAL` | *(ninguno)* | Componente sin subtipo. |
| `LLANTA` | `ancho_mm|perfil|diametro_pulgadas|indice_carga|indice_velocidad` | `205|55|16|91|V` |
| `RIN` | `diametro_pulgadas|ancho_pulgadas|pcd|material` | `ALEACION` o `ACERO` |
| `MOTOR` | `cilindraje_cc|cilindros|combustible|potencia_hp` | `GASOLINA`, `DIESEL`... |
| `TRANSMISION` | `tipo_caja|velocidades` | `MANUAL` o `AUTOMATICA` |

```
GENERAL|5|Pastillas de freno delanteras|PF-1234|2|1|GENERICO|ACTIVO
LLANTA|8|Llanta Michelin Primacy 4|MP4-2055516|4|3|ORIGINAL|ACTIVO|205|55|16|91|V
RIN|10|Rin de aleación 17 pulgadas|RA-17-5114|7|4|GENERICO|ACTIVO|17|7.5|5x114.3|ALEACION
MOTOR|12|Motor 1.6L 16V reacondicionado|MT-16-16V|8|2|ORIGINAL|ACTIVO|1600|4|GASOLINA|110
TRANSMISION|14|Caja automática 6 velocidades|TR-A6-001|6|5|ORIGINAL|ACTIVO|AUTOMATICA|6
```

### `vehicles.csv` (herencia: Vehículo → Coche / Motocicleta)
Campos comunes: `tipo|id|marca|modelo|anio|estado`

| Tipo | Campos propios (después de los comunes) |
|---|---|
| `COCHE` | `numero_puertas|carroceria` |
| `MOTOCICLETA` | `cilindraje_cc|tipo_moto` |

```
COCHE|1|Chevrolet|Spark GT|2018|ACTIVO|5|HATCHBACK
MOTOCICLETA|4|Yamaha|FZ 150|2021|ACTIVO|150|NAKED
```

### `compatibility.csv` (qué componente sirve en qué vehículo)
`id_componente|id_vehiculo`

- No tiene identificador propio. La pareja no se puede repetir.

```
5|1
```

## Inventario

### `inventory.csv` (oferta de un proveedor para un componente)
`id|id_proveedor|id_componente|precio|stock|valor_envio|garantia_meses|condicion|estado`

- `id_proveedor` es el identificador de un `PROVEEDOR` en `partners.csv`.
- `condicion`: `NUEVO`, `USADO` o `REACONDICIONADO`.
- Un proveedor no repite el mismo componente.
- Costo total de una oferta = `precio + valor_envio`.

```
1|1|5|185000|8|12000|12|NUEVO|ACTIVO
```

## Transacciones (paso 4, solo si alcanza el tiempo)

Los archivos existen vacíos. El formato es una propuesta para que Jahdiel
la confirme **(\*)**.

### `transactions.csv`
`id|id_usuario|fecha|estado|total`

- `estado`: `PENDIENTE`, `PAGADA` o `CANCELADA`.

### `transaction_details.csv`
`id|id_transaccion|id_inventario|cantidad|precio_unitario`

- `precio_unitario` es el precio de la oferta en el momento de la compra.
