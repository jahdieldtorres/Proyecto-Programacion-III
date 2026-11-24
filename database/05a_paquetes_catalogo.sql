-- =====================================================================
-- ORIGEN · Script 05a · Paquetes PL/SQL del catálogo
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
-- @author Jahdiel Torres, Álvaro Aramendiz
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5).
-- Se corre DESPUÉS de 01 → 02 → 03 → 04. Se puede repetir las veces que
-- haga falta (CREATE OR REPLACE). Si se vuelve a correr 01_tablas.sql, los
-- paquetes quedan inválidos: hay que correr este script otra vez.
--
-- Idea: todas las consultas y cambios a las tablas viven aquí, en PL/SQL.
-- Los DAO de Java (dao/oracle) no escriben SELECT, INSERT ni UPDATE:
-- solo llaman estas funciones con CallableStatement.
--
-- Reglas para todos los paquetes:
--   1. Un paquete por tabla principal (PKG_USERS, PKG_BRANDS, ...).
--   2. TODO es una FUNCIÓN y Java siempre lee el resultado en el primer ?:
--        - Consultas: devuelven SYS_REFCURSOR (Java lo recibe como ResultSet).
--        - INSERT:    devuelven NUMBER con el ID nuevo (RETURNING ID INTO),
--                     porque los ID son IDENTITY.
--        - UPDATE:    devuelven NUMBER con las filas cambiadas (SQL%ROWCOUNT).
--                     Si es 0, el registro no existía y el DAO avisa.
--   3. Ninguna función tiene parámetros OUT: lo que se devuelve es el RETURN.
--   4. Sin COMMIT dentro del paquete: lo confirma Java (la conexión del
--      DAO confirma sola al terminar cada llamada).
--   5. Sin WHEN OTHERS: los errores tienen que llegar a Java (así funciona
--      el error -20001 de stock del disparador).
--   6. Ninguna función descuenta stock: lo hace el disparador.
--   7. "Eliminar" es poner STATUS = 'INACTIVO', no DELETE. Solo las tablas que
--      no tienen STATUS (BRANDS, CATEGORIES, COMPATIBILITY, INVENTORY_IMAGES)
--      borran de verdad; las llaves foráneas protegen los datos en uso.
--
-- Paquetes de este archivo:
--   PKG_USERS             Usuarios (ejemplo de referencia para los demás).
--   PKG_BRANDS            Marcas de repuestos.
--   PKG_CATEGORIES        Categorías del catálogo.
--   PKG_COMPONENTS        Componentes y sus subtipos (llanta, rin, motor, transmisión).
--   PKG_VEHICLES          Vehículos y sus subtipos (coche, motocicleta).
--   PKG_COMPATIBILITY     Qué componente sirve para qué vehículo.
--   PKG_INVENTORY_IMAGES  Fotos de cada oferta de inventario.
--
-- Para comprobar que todo quedó bien:
--   SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS
--   WHERE OBJECT_TYPE LIKE 'PACKAGE%';      -- todos deben decir VALID
-- =====================================================================


-- =====================================================================
-- PKG_USERS · Usuarios
-- Lo usa autoparts.dao.oracle.UserOracleDAO.
-- =====================================================================

-- Especificación: lo que el paquete ofrece (como una interfaz en Java)
CREATE OR REPLACE PACKAGE PKG_USERS AS

    -- Todos los usuarios, ordenados por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- El usuario con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- El usuario con ese correo, sin importar mayúsculas (cero o una fila).
    FUNCTION FIND_BY_EMAIL (p_email IN VARCHAR2) RETURN SYS_REFCURSOR;

    -- Guarda un usuario nuevo y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_USER (
        p_name     IN VARCHAR2,
        p_email    IN VARCHAR2,
        p_hash     IN VARCHAR2,
        p_role     IN VARCHAR2,
        p_status   IN VARCHAR2,
        p_reg_date IN DATE
    ) RETURN NUMBER;

    -- Cambia los datos de un usuario y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID).
    FUNCTION UPDATE_USER (
        p_id     IN NUMBER,
        p_name   IN VARCHAR2,
        p_email  IN VARCHAR2,
        p_hash   IN VARCHAR2,
        p_role   IN VARCHAR2,
        p_status IN VARCHAR2
    ) RETURN NUMBER;

END PKG_USERS;
/

-- Cuerpo: cómo se hace cada cosa
CREATE OR REPLACE PACKAGE BODY PKG_USERS AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_users SYS_REFCURSOR;
    BEGIN
        OPEN c_users FOR
            SELECT ID, NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE
            FROM USERS
            ORDER BY ID;
        RETURN c_users;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_user SYS_REFCURSOR;
    BEGIN
        OPEN c_user FOR
            SELECT ID, NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE
            FROM USERS
            WHERE ID = p_id;
        RETURN c_user;
    END FIND_BY_ID;

    FUNCTION FIND_BY_EMAIL (p_email IN VARCHAR2) RETURN SYS_REFCURSOR IS
        c_user SYS_REFCURSOR;
    BEGIN
        OPEN c_user FOR
            SELECT ID, NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE
            FROM USERS
            WHERE LOWER(EMAIL) = LOWER(p_email);
        RETURN c_user;
    END FIND_BY_EMAIL;

    FUNCTION INSERT_USER (
        p_name     IN VARCHAR2,
        p_email    IN VARCHAR2,
        p_hash     IN VARCHAR2,
        p_role     IN VARCHAR2,
        p_status   IN VARCHAR2,
        p_reg_date IN DATE
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO USERS (NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, REGISTRATION_DATE)
        VALUES (p_name, p_email, p_hash, p_role, p_status, p_reg_date)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_USER;

    FUNCTION UPDATE_USER (
        p_id     IN NUMBER,
        p_name   IN VARCHAR2,
        p_email  IN VARCHAR2,
        p_hash   IN VARCHAR2,
        p_role   IN VARCHAR2,
        p_status IN VARCHAR2
    ) RETURN NUMBER IS
    BEGIN
        UPDATE USERS
        SET NAME = p_name,
            EMAIL = p_email,
            PASSWORD_HASH = p_hash,
            ROLE = p_role,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_USER;

END PKG_USERS;
/

-- =====================================================================
-- PKG_INVENTORY_IMAGES · Fotos de cada oferta de inventario
-- Lo usa autoparts.dao.oracle.InventoryImageOracleDAO.
--
-- Regla que cuida este paquete: una oferta tiene como máximo UNA foto
-- principal (IS_MAIN = 'S'). Al guardar o actualizar una foto como
-- principal, las otras de la misma oferta se desmarcan dentro de la misma
-- función. Como Java confirma después de cada llamada y Oracle deshace toda
-- la llamada si algo falla, las dos operaciones quedan juntas o no quedan.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_INVENTORY_IMAGES AS

    -- Las fotos de una oferta: primero la principal, después las demás
    -- por su orden.
    FUNCTION LIST_BY_INVENTORY (p_inventory_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- La foto que se muestra de la oferta: la principal o, si ninguna lo
    -- es, la primera por orden (cero o una fila).
    FUNCTION FIND_MAIN (p_inventory_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- La foto con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una foto nueva y devuelve el ID que le dio Oracle.
    -- p_is_main es 'S' o 'N'. Si es 'S', las otras fotos de la oferta
    -- dejan de ser la principal.
    FUNCTION INSERT_IMAGE (
        p_inventory_id IN NUMBER,
        p_file_name    IN VARCHAR2,
        p_caption      IN VARCHAR2,
        p_is_main      IN VARCHAR2,
        p_sort_order   IN NUMBER
    ) RETURN NUMBER;

    -- Cambia los datos de una foto y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID). Si pasa a ser la principal,
    -- las otras fotos de la oferta dejan de serlo.
    FUNCTION UPDATE_IMAGE (
        p_id         IN NUMBER,
        p_file_name  IN VARCHAR2,
        p_caption    IN VARCHAR2,
        p_is_main    IN VARCHAR2,
        p_sort_order IN NUMBER
    ) RETURN NUMBER;

    -- Elimina el registro de la foto y devuelve cuántas filas borró.
    -- Aquí sí es DELETE: la tabla no tiene STATUS y la foto no tiene
    -- historial que cuidar (el archivo en disco lo maneja ImageStorage).
    FUNCTION DELETE_IMAGE (p_id IN NUMBER) RETURN NUMBER;

END PKG_INVENTORY_IMAGES;
/

CREATE OR REPLACE PACKAGE BODY PKG_INVENTORY_IMAGES AS

    FUNCTION LIST_BY_INVENTORY (p_inventory_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_images SYS_REFCURSOR;
    BEGIN
        -- 'S' es mayor que 'N', por eso IS_MAIN DESC deja la principal primero.
        OPEN c_images FOR
            SELECT ID, INVENTORY_ID, FILE_NAME, CAPTION, IS_MAIN, SORT_ORDER
            FROM INVENTORY_IMAGES
            WHERE INVENTORY_ID = p_inventory_id
            ORDER BY IS_MAIN DESC, SORT_ORDER, ID;
        RETURN c_images;
    END LIST_BY_INVENTORY;

    FUNCTION FIND_MAIN (p_inventory_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_image SYS_REFCURSOR;
    BEGIN
        OPEN c_image FOR
            SELECT ID, INVENTORY_ID, FILE_NAME, CAPTION, IS_MAIN, SORT_ORDER
            FROM INVENTORY_IMAGES
            WHERE INVENTORY_ID = p_inventory_id
            ORDER BY IS_MAIN DESC, SORT_ORDER, ID
            FETCH FIRST 1 ROWS ONLY;
        RETURN c_image;
    END FIND_MAIN;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_image SYS_REFCURSOR;
    BEGIN
        OPEN c_image FOR
            SELECT ID, INVENTORY_ID, FILE_NAME, CAPTION, IS_MAIN, SORT_ORDER
            FROM INVENTORY_IMAGES
            WHERE ID = p_id;
        RETURN c_image;
    END FIND_BY_ID;

    FUNCTION INSERT_IMAGE (
        p_inventory_id IN NUMBER,
        p_file_name    IN VARCHAR2,
        p_caption      IN VARCHAR2,
        p_is_main      IN VARCHAR2,
        p_sort_order   IN NUMBER
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO INVENTORY_IMAGES (INVENTORY_ID, FILE_NAME, CAPTION, IS_MAIN, SORT_ORDER)
        VALUES (p_inventory_id, p_file_name, p_caption, p_is_main, p_sort_order)
        RETURNING ID INTO v_id;

        IF p_is_main = 'S' THEN
            UPDATE INVENTORY_IMAGES
            SET IS_MAIN = 'N'
            WHERE INVENTORY_ID = p_inventory_id
              AND ID <> v_id;
        END IF;

        RETURN v_id;
    END INSERT_IMAGE;

    FUNCTION UPDATE_IMAGE (
        p_id         IN NUMBER,
        p_file_name  IN VARCHAR2,
        p_caption    IN VARCHAR2,
        p_is_main    IN VARCHAR2,
        p_sort_order IN NUMBER
    ) RETURN NUMBER IS
        v_rows NUMBER;
    BEGIN
        UPDATE INVENTORY_IMAGES
        SET FILE_NAME = p_file_name,
            CAPTION = p_caption,
            IS_MAIN = p_is_main,
            SORT_ORDER = p_sort_order
        WHERE ID = p_id;
        v_rows := SQL%ROWCOUNT;

        IF v_rows > 0 AND p_is_main = 'S' THEN
            UPDATE INVENTORY_IMAGES
            SET IS_MAIN = 'N'
            WHERE INVENTORY_ID = (SELECT INVENTORY_ID FROM INVENTORY_IMAGES WHERE ID = p_id)
              AND ID <> p_id;
        END IF;

        RETURN v_rows;
    END UPDATE_IMAGE;

    FUNCTION DELETE_IMAGE (p_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        DELETE FROM INVENTORY_IMAGES WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END DELETE_IMAGE;

END PKG_INVENTORY_IMAGES;
/

-- =====================================================================
-- PKG_BRANDS · Marcas de repuestos
-- Lo usa autoparts.dao.oracle.BrandOracleDAO.
-- Eliminar es DELETE real: la tabla no tiene STATUS. Si un componente usa
-- la marca, Oracle rechaza el borrado por la llave foránea (ORA-02292) y
-- el error llega a Java.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_BRANDS AS

    -- Todas las marcas, ordenadas por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- La marca con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una marca nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_BRAND (p_name IN VARCHAR2) RETURN NUMBER;

    -- Cambia el nombre y devuelve cuántas filas cambió (0 si no existe).
    FUNCTION UPDATE_BRAND (p_id IN NUMBER, p_name IN VARCHAR2) RETURN NUMBER;

    -- Borra la marca y devuelve cuántas filas borró.
    FUNCTION DELETE_BRAND (p_id IN NUMBER) RETURN NUMBER;

END PKG_BRANDS;
/

CREATE OR REPLACE PACKAGE BODY PKG_BRANDS AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_brands SYS_REFCURSOR;
    BEGIN
        OPEN c_brands FOR
            SELECT ID, NAME FROM BRANDS ORDER BY ID;
        RETURN c_brands;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_brand SYS_REFCURSOR;
    BEGIN
        OPEN c_brand FOR
            SELECT ID, NAME FROM BRANDS WHERE ID = p_id;
        RETURN c_brand;
    END FIND_BY_ID;

    FUNCTION INSERT_BRAND (p_name IN VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO BRANDS (NAME) VALUES (p_name) RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_BRAND;

    FUNCTION UPDATE_BRAND (p_id IN NUMBER, p_name IN VARCHAR2) RETURN NUMBER IS
    BEGIN
        UPDATE BRANDS SET NAME = p_name WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_BRAND;

    FUNCTION DELETE_BRAND (p_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        DELETE FROM BRANDS WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END DELETE_BRAND;

END PKG_BRANDS;
/


-- =====================================================================
-- PKG_CATEGORIES · Categorías del catálogo
-- Lo usa autoparts.dao.oracle.CategoryOracleDAO.
-- Igual que las marcas: DELETE real y la llave foránea protege las que
-- tienen componentes.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_CATEGORIES AS

    -- Todas las categorías, ordenadas por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- La categoría con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una categoría nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_CATEGORY (p_name IN VARCHAR2) RETURN NUMBER;

    -- Cambia el nombre y devuelve cuántas filas cambió (0 si no existe).
    FUNCTION UPDATE_CATEGORY (p_id IN NUMBER, p_name IN VARCHAR2) RETURN NUMBER;

    -- Borra la categoría y devuelve cuántas filas borró.
    FUNCTION DELETE_CATEGORY (p_id IN NUMBER) RETURN NUMBER;

END PKG_CATEGORIES;
/

CREATE OR REPLACE PACKAGE BODY PKG_CATEGORIES AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_categories SYS_REFCURSOR;
    BEGIN
        OPEN c_categories FOR
            SELECT ID, NAME FROM CATEGORIES ORDER BY ID;
        RETURN c_categories;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_category SYS_REFCURSOR;
    BEGIN
        OPEN c_category FOR
            SELECT ID, NAME FROM CATEGORIES WHERE ID = p_id;
        RETURN c_category;
    END FIND_BY_ID;

    FUNCTION INSERT_CATEGORY (p_name IN VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO CATEGORIES (NAME) VALUES (p_name) RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_CATEGORY;

    FUNCTION UPDATE_CATEGORY (p_id IN NUMBER, p_name IN VARCHAR2) RETURN NUMBER IS
    BEGIN
        UPDATE CATEGORIES SET NAME = p_name WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_CATEGORY;

    FUNCTION DELETE_CATEGORY (p_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        DELETE FROM CATEGORIES WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END DELETE_CATEGORY;

END PKG_CATEGORIES;
/


-- =====================================================================
-- PKG_COMPONENTS · Componentes (repuestos) y sus subtipos
-- Lo usa autoparts.dao.oracle.ComponentOracleDAO.
--
-- Cada componente ocupa COMPONENTS y, si es llanta, rin, motor o
-- transmisión, también TIRES, RIMS, ENGINES o TRANSMISSIONS con el mismo
-- ID. Las funciones de este paquete tocan UNA tabla cada una; el DAO las
-- llama en una transacción (setAutoCommit(false), commit y rollback en
-- Java) para que se guarden las dos filas o ninguna.
--
-- En las funciones de los subtipos el ID del componente va de último, así
-- INSERT_xxx y UPDATE_xxx reciben los mismos parámetros. Devuelven las
-- filas afectadas: si UPDATE_xxx devuelve 0, el componente todavía no tenía
-- fila de subtipo y el DAO la crea con INSERT_xxx.
--
-- Los componentes NO se borran (TRG_COMPONENTS_NO_DELETE lo impide): se
-- pasan a INACTIVO con DEACTIVATE_COMPONENT.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_COMPONENTS AS

    -- Todos los componentes con su marca, categoría y datos de subtipo,
    -- ordenados por ID. Los datos de los subtipos que no aplican vienen NULL.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- El componente con ese ID, con las mismas columnas (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda la fila de COMPONENTS y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_COMPONENT (
        p_type        IN VARCHAR2,
        p_name        IN VARCHAR2,
        p_reference   IN VARCHAR2,
        p_brand_id    IN NUMBER,
        p_category_id IN NUMBER,
        p_origin      IN VARCHAR2,
        p_description IN VARCHAR2,
        p_status      IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia la fila de COMPONENTS y devuelve cuántas filas cambió.
    FUNCTION UPDATE_COMPONENT (
        p_type        IN VARCHAR2,
        p_name        IN VARCHAR2,
        p_reference   IN VARCHAR2,
        p_brand_id    IN NUMBER,
        p_category_id IN NUMBER,
        p_origin      IN VARCHAR2,
        p_description IN VARCHAR2,
        p_status      IN VARCHAR2,
        p_id          IN NUMBER
    ) RETURN NUMBER;

    -- Borrado lógico: STATUS = 'INACTIVO'. Devuelve cuántas filas cambió.
    FUNCTION DEACTIVATE_COMPONENT (p_id IN NUMBER) RETURN NUMBER;

    -- Llantas
    FUNCTION INSERT_TIRE (
        p_width_mm IN NUMBER, p_aspect_ratio IN NUMBER, p_diameter_inches IN NUMBER,
        p_load_index IN NUMBER, p_speed_index IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_TIRE (
        p_width_mm IN NUMBER, p_aspect_ratio IN NUMBER, p_diameter_inches IN NUMBER,
        p_load_index IN NUMBER, p_speed_index IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER;

    -- Rines
    FUNCTION INSERT_RIM (
        p_diameter_inches IN NUMBER, p_width_inches IN NUMBER, p_pcd IN VARCHAR2,
        p_material IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_RIM (
        p_diameter_inches IN NUMBER, p_width_inches IN NUMBER, p_pcd IN VARCHAR2,
        p_material IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER;

    -- Motores
    FUNCTION INSERT_ENGINE (
        p_displacement_cc IN NUMBER, p_cylinders IN NUMBER, p_fuel IN VARCHAR2,
        p_horsepower IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_ENGINE (
        p_displacement_cc IN NUMBER, p_cylinders IN NUMBER, p_fuel IN VARCHAR2,
        p_horsepower IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER;

    -- Transmisiones
    FUNCTION INSERT_TRANSMISSION (
        p_transmission_type IN VARCHAR2, p_number_of_speeds IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_TRANSMISSION (
        p_transmission_type IN VARCHAR2, p_number_of_speeds IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER;

END PKG_COMPONENTS;
/

CREATE OR REPLACE PACKAGE BODY PKG_COMPONENTS AS

    -- Lee el componente con su marca, su categoría y los datos de su subtipo.
    -- Los LEFT JOIN dejan en NULL las columnas de los subtipos que no aplican.
    -- LIST_ALL y FIND_BY_ID tienen la misma consulta; solo cambia el WHERE.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_components SYS_REFCURSOR;
    BEGIN
        OPEN c_components FOR
            SELECT C.ID, C.COMPONENT_TYPE, C.NAME, C.REFERENCE, C.ORIGIN, C.STATUS,
                   C.DESCRIPTION,
                   B.ID AS BRAND_ID, B.NAME AS BRAND_NAME,
                   G.ID AS CATEGORY_ID, G.NAME AS CATEGORY_NAME,
                   T.WIDTH_MM, T.ASPECT_RATIO, T.DIAMETER_INCHES AS TIRE_DIAMETER,
                   T.LOAD_INDEX, T.SPEED_INDEX,
                   R.DIAMETER_INCHES AS RIM_DIAMETER, R.WIDTH_INCHES AS RIM_WIDTH,
                   R.PCD, R.MATERIAL,
                   E.DISPLACEMENT_CC, E.CYLINDERS, E.FUEL, E.HORSEPOWER,
                   X.TRANSMISSION_TYPE, X.NUMBER_OF_SPEEDS
            FROM COMPONENTS C
            JOIN BRANDS B ON B.ID = C.BRAND_ID
            JOIN CATEGORIES G ON G.ID = C.CATEGORY_ID
            LEFT JOIN TIRES T ON T.COMPONENT_ID = C.ID
            LEFT JOIN RIMS R ON R.COMPONENT_ID = C.ID
            LEFT JOIN ENGINES E ON E.COMPONENT_ID = C.ID
            LEFT JOIN TRANSMISSIONS X ON X.COMPONENT_ID = C.ID
            ORDER BY C.ID;
        RETURN c_components;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_component SYS_REFCURSOR;
    BEGIN
        OPEN c_component FOR
            SELECT C.ID, C.COMPONENT_TYPE, C.NAME, C.REFERENCE, C.ORIGIN, C.STATUS,
                   C.DESCRIPTION,
                   B.ID AS BRAND_ID, B.NAME AS BRAND_NAME,
                   G.ID AS CATEGORY_ID, G.NAME AS CATEGORY_NAME,
                   T.WIDTH_MM, T.ASPECT_RATIO, T.DIAMETER_INCHES AS TIRE_DIAMETER,
                   T.LOAD_INDEX, T.SPEED_INDEX,
                   R.DIAMETER_INCHES AS RIM_DIAMETER, R.WIDTH_INCHES AS RIM_WIDTH,
                   R.PCD, R.MATERIAL,
                   E.DISPLACEMENT_CC, E.CYLINDERS, E.FUEL, E.HORSEPOWER,
                   X.TRANSMISSION_TYPE, X.NUMBER_OF_SPEEDS
            FROM COMPONENTS C
            JOIN BRANDS B ON B.ID = C.BRAND_ID
            JOIN CATEGORIES G ON G.ID = C.CATEGORY_ID
            LEFT JOIN TIRES T ON T.COMPONENT_ID = C.ID
            LEFT JOIN RIMS R ON R.COMPONENT_ID = C.ID
            LEFT JOIN ENGINES E ON E.COMPONENT_ID = C.ID
            LEFT JOIN TRANSMISSIONS X ON X.COMPONENT_ID = C.ID
            WHERE C.ID = p_id;
        RETURN c_component;
    END FIND_BY_ID;

    FUNCTION INSERT_COMPONENT (
        p_type        IN VARCHAR2,
        p_name        IN VARCHAR2,
        p_reference   IN VARCHAR2,
        p_brand_id    IN NUMBER,
        p_category_id IN NUMBER,
        p_origin      IN VARCHAR2,
        p_description IN VARCHAR2,
        p_status      IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO COMPONENTS (COMPONENT_TYPE, NAME, REFERENCE, BRAND_ID, CATEGORY_ID, ORIGIN, DESCRIPTION, STATUS)
        VALUES (p_type, p_name, p_reference, p_brand_id, p_category_id, p_origin, p_description, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_COMPONENT;

    FUNCTION UPDATE_COMPONENT (
        p_type        IN VARCHAR2,
        p_name        IN VARCHAR2,
        p_reference   IN VARCHAR2,
        p_brand_id    IN NUMBER,
        p_category_id IN NUMBER,
        p_origin      IN VARCHAR2,
        p_description IN VARCHAR2,
        p_status      IN VARCHAR2,
        p_id          IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE COMPONENTS
        SET COMPONENT_TYPE = p_type,
            NAME = p_name,
            REFERENCE = p_reference,
            BRAND_ID = p_brand_id,
            CATEGORY_ID = p_category_id,
            ORIGIN = p_origin,
            DESCRIPTION = p_description,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_COMPONENT;

    FUNCTION DEACTIVATE_COMPONENT (p_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        UPDATE COMPONENTS SET STATUS = 'INACTIVO' WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END DEACTIVATE_COMPONENT;

    FUNCTION INSERT_TIRE (
        p_width_mm IN NUMBER, p_aspect_ratio IN NUMBER, p_diameter_inches IN NUMBER,
        p_load_index IN NUMBER, p_speed_index IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO TIRES (WIDTH_MM, ASPECT_RATIO, DIAMETER_INCHES, LOAD_INDEX, SPEED_INDEX, COMPONENT_ID)
        VALUES (p_width_mm, p_aspect_ratio, p_diameter_inches, p_load_index, p_speed_index, p_component_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_TIRE;

    FUNCTION UPDATE_TIRE (
        p_width_mm IN NUMBER, p_aspect_ratio IN NUMBER, p_diameter_inches IN NUMBER,
        p_load_index IN NUMBER, p_speed_index IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE TIRES
        SET WIDTH_MM = p_width_mm,
            ASPECT_RATIO = p_aspect_ratio,
            DIAMETER_INCHES = p_diameter_inches,
            LOAD_INDEX = p_load_index,
            SPEED_INDEX = p_speed_index
        WHERE COMPONENT_ID = p_component_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_TIRE;

    FUNCTION INSERT_RIM (
        p_diameter_inches IN NUMBER, p_width_inches IN NUMBER, p_pcd IN VARCHAR2,
        p_material IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO RIMS (DIAMETER_INCHES, WIDTH_INCHES, PCD, MATERIAL, COMPONENT_ID)
        VALUES (p_diameter_inches, p_width_inches, p_pcd, p_material, p_component_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_RIM;

    FUNCTION UPDATE_RIM (
        p_diameter_inches IN NUMBER, p_width_inches IN NUMBER, p_pcd IN VARCHAR2,
        p_material IN VARCHAR2, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE RIMS
        SET DIAMETER_INCHES = p_diameter_inches,
            WIDTH_INCHES = p_width_inches,
            PCD = p_pcd,
            MATERIAL = p_material
        WHERE COMPONENT_ID = p_component_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_RIM;

    FUNCTION INSERT_ENGINE (
        p_displacement_cc IN NUMBER, p_cylinders IN NUMBER, p_fuel IN VARCHAR2,
        p_horsepower IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO ENGINES (DISPLACEMENT_CC, CYLINDERS, FUEL, HORSEPOWER, COMPONENT_ID)
        VALUES (p_displacement_cc, p_cylinders, p_fuel, p_horsepower, p_component_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_ENGINE;

    FUNCTION UPDATE_ENGINE (
        p_displacement_cc IN NUMBER, p_cylinders IN NUMBER, p_fuel IN VARCHAR2,
        p_horsepower IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE ENGINES
        SET DISPLACEMENT_CC = p_displacement_cc,
            CYLINDERS = p_cylinders,
            FUEL = p_fuel,
            HORSEPOWER = p_horsepower
        WHERE COMPONENT_ID = p_component_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_ENGINE;

    FUNCTION INSERT_TRANSMISSION (
        p_transmission_type IN VARCHAR2, p_number_of_speeds IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO TRANSMISSIONS (TRANSMISSION_TYPE, NUMBER_OF_SPEEDS, COMPONENT_ID)
        VALUES (p_transmission_type, p_number_of_speeds, p_component_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_TRANSMISSION;

    FUNCTION UPDATE_TRANSMISSION (
        p_transmission_type IN VARCHAR2, p_number_of_speeds IN NUMBER, p_component_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE TRANSMISSIONS
        SET TRANSMISSION_TYPE = p_transmission_type,
            NUMBER_OF_SPEEDS = p_number_of_speeds
        WHERE COMPONENT_ID = p_component_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_TRANSMISSION;

END PKG_COMPONENTS;
/


-- =====================================================================
-- PKG_VEHICLES · Vehículos y sus subtipos (coche o motocicleta)
-- Lo usa autoparts.dao.oracle.VehicleOracleDAO.
--
-- Igual que PKG_COMPONENTS: cada función toca una tabla y el DAO las llama
-- en una transacción. El ID del vehículo va de último en las funciones de
-- los subtipos. Los vehículos no se borran: se pasan a INACTIVO.
--
-- La marca del vehículo es un texto (VEHICLES.BRAND_NAME). Si coincide con
-- una marca de BRANDS se devuelve su ID y, si no, 0.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_VEHICLES AS

    -- Todos los vehículos con los datos de su subtipo, ordenados por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- El vehículo con ese ID, con las mismas columnas (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda la fila de VEHICLES y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_VEHICLE (
        p_type       IN VARCHAR2,
        p_brand_name IN VARCHAR2,
        p_model_name IN VARCHAR2,
        p_year       IN NUMBER,
        p_status     IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia la fila de VEHICLES y devuelve cuántas filas cambió.
    FUNCTION UPDATE_VEHICLE (
        p_type       IN VARCHAR2,
        p_brand_name IN VARCHAR2,
        p_model_name IN VARCHAR2,
        p_year       IN NUMBER,
        p_status     IN VARCHAR2,
        p_id         IN NUMBER
    ) RETURN NUMBER;

    -- Borrado lógico: STATUS = 'INACTIVO'. Devuelve cuántas filas cambió.
    FUNCTION DEACTIVATE_VEHICLE (p_id IN NUMBER) RETURN NUMBER;

    -- Coches
    FUNCTION INSERT_CAR (
        p_num_doors IN NUMBER, p_car_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_CAR (
        p_num_doors IN NUMBER, p_car_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER;

    -- Motocicletas
    FUNCTION INSERT_MOTORCYCLE (
        p_displacement_cc IN NUMBER, p_motorcycle_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_MOTORCYCLE (
        p_displacement_cc IN NUMBER, p_motorcycle_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER;

END PKG_VEHICLES;
/

CREATE OR REPLACE PACKAGE BODY PKG_VEHICLES AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_vehicles SYS_REFCURSOR;
    BEGIN
        OPEN c_vehicles FOR
            SELECT V.ID, V.VEHICLE_TYPE, V.BRAND_NAME, NVL(B.ID, 0) AS BRAND_ID,
                   V.MODEL_NAME, V.YEAR_RELEASE, V.STATUS,
                   C.NUM_DOORS, C.CAR_TYPE, M.DISPLACEMENT_CC, M.MOTORCYCLE_TYPE
            FROM VEHICLES V
            LEFT JOIN BRANDS B ON B.NAME = V.BRAND_NAME
            LEFT JOIN CARS C ON C.VEHICLE_ID = V.ID
            LEFT JOIN MOTORCYCLES M ON M.VEHICLE_ID = V.ID
            ORDER BY V.ID;
        RETURN c_vehicles;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_vehicle SYS_REFCURSOR;
    BEGIN
        OPEN c_vehicle FOR
            SELECT V.ID, V.VEHICLE_TYPE, V.BRAND_NAME, NVL(B.ID, 0) AS BRAND_ID,
                   V.MODEL_NAME, V.YEAR_RELEASE, V.STATUS,
                   C.NUM_DOORS, C.CAR_TYPE, M.DISPLACEMENT_CC, M.MOTORCYCLE_TYPE
            FROM VEHICLES V
            LEFT JOIN BRANDS B ON B.NAME = V.BRAND_NAME
            LEFT JOIN CARS C ON C.VEHICLE_ID = V.ID
            LEFT JOIN MOTORCYCLES M ON M.VEHICLE_ID = V.ID
            WHERE V.ID = p_id;
        RETURN c_vehicle;
    END FIND_BY_ID;

    FUNCTION INSERT_VEHICLE (
        p_type       IN VARCHAR2,
        p_brand_name IN VARCHAR2,
        p_model_name IN VARCHAR2,
        p_year       IN NUMBER,
        p_status     IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO VEHICLES (VEHICLE_TYPE, BRAND_NAME, MODEL_NAME, YEAR_RELEASE, STATUS)
        VALUES (p_type, p_brand_name, p_model_name, p_year, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_VEHICLE;

    FUNCTION UPDATE_VEHICLE (
        p_type       IN VARCHAR2,
        p_brand_name IN VARCHAR2,
        p_model_name IN VARCHAR2,
        p_year       IN NUMBER,
        p_status     IN VARCHAR2,
        p_id         IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE VEHICLES
        SET VEHICLE_TYPE = p_type,
            BRAND_NAME = p_brand_name,
            MODEL_NAME = p_model_name,
            YEAR_RELEASE = p_year,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_VEHICLE;

    FUNCTION DEACTIVATE_VEHICLE (p_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        UPDATE VEHICLES SET STATUS = 'INACTIVO' WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END DEACTIVATE_VEHICLE;

    FUNCTION INSERT_CAR (
        p_num_doors IN NUMBER, p_car_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO CARS (NUM_DOORS, CAR_TYPE, VEHICLE_ID)
        VALUES (p_num_doors, p_car_type, p_vehicle_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_CAR;

    FUNCTION UPDATE_CAR (
        p_num_doors IN NUMBER, p_car_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE CARS
        SET NUM_DOORS = p_num_doors,
            CAR_TYPE = p_car_type
        WHERE VEHICLE_ID = p_vehicle_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_CAR;

    FUNCTION INSERT_MOTORCYCLE (
        p_displacement_cc IN NUMBER, p_motorcycle_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO MOTORCYCLES (DISPLACEMENT_CC, MOTORCYCLE_TYPE, VEHICLE_ID)
        VALUES (p_displacement_cc, p_motorcycle_type, p_vehicle_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_MOTORCYCLE;

    FUNCTION UPDATE_MOTORCYCLE (
        p_displacement_cc IN NUMBER, p_motorcycle_type IN VARCHAR2, p_vehicle_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE MOTORCYCLES
        SET DISPLACEMENT_CC = p_displacement_cc,
            MOTORCYCLE_TYPE = p_motorcycle_type
        WHERE VEHICLE_ID = p_vehicle_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_MOTORCYCLE;

END PKG_VEHICLES;
/


-- =====================================================================
-- PKG_COMPATIBILITY · Qué componente sirve para qué vehículo
-- Lo usa autoparts.dao.oracle.CompatibilityOracleDAO.
-- La llave primaria es (COMPONENT_ID, VEHICLE_ID): enlazar dos veces el
-- mismo par hace que Oracle rechace la fila (ORA-00001) y el error llega a
-- Java. Quitar un enlace es DELETE real: la tabla no tiene STATUS.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_COMPATIBILITY AS

    -- Los ID de los vehículos compatibles con un componente, ordenados.
    FUNCTION LIST_VEHICLE_IDS (p_component_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Los ID de los componentes compatibles con un vehículo, ordenados.
    FUNCTION LIST_COMPONENT_IDS (p_vehicle_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Enlaza un componente con un vehículo y devuelve las filas guardadas.
    FUNCTION LINK_COMPATIBILITY (p_component_id IN NUMBER, p_vehicle_id IN NUMBER) RETURN NUMBER;

    -- Quita el enlace y devuelve las filas borradas (0 si no existía).
    FUNCTION UNLINK_COMPATIBILITY (p_component_id IN NUMBER, p_vehicle_id IN NUMBER) RETURN NUMBER;

END PKG_COMPATIBILITY;
/

CREATE OR REPLACE PACKAGE BODY PKG_COMPATIBILITY AS

    FUNCTION LIST_VEHICLE_IDS (p_component_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_ids SYS_REFCURSOR;
    BEGIN
        OPEN c_ids FOR
            SELECT VEHICLE_ID FROM COMPATIBILITY
            WHERE COMPONENT_ID = p_component_id
            ORDER BY VEHICLE_ID;
        RETURN c_ids;
    END LIST_VEHICLE_IDS;

    FUNCTION LIST_COMPONENT_IDS (p_vehicle_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_ids SYS_REFCURSOR;
    BEGIN
        OPEN c_ids FOR
            SELECT COMPONENT_ID FROM COMPATIBILITY
            WHERE VEHICLE_ID = p_vehicle_id
            ORDER BY COMPONENT_ID;
        RETURN c_ids;
    END LIST_COMPONENT_IDS;

    FUNCTION LINK_COMPATIBILITY (p_component_id IN NUMBER, p_vehicle_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        INSERT INTO COMPATIBILITY (COMPONENT_ID, VEHICLE_ID)
        VALUES (p_component_id, p_vehicle_id);
        RETURN SQL%ROWCOUNT;
    END LINK_COMPATIBILITY;

    FUNCTION UNLINK_COMPATIBILITY (p_component_id IN NUMBER, p_vehicle_id IN NUMBER) RETURN NUMBER IS
    BEGIN
        DELETE FROM COMPATIBILITY
        WHERE COMPONENT_ID = p_component_id AND VEHICLE_ID = p_vehicle_id;
        RETURN SQL%ROWCOUNT;
    END UNLINK_COMPATIBILITY;

END PKG_COMPATIBILITY;
/

-- ---------------------------------------------------------------------
-- Revisión: errores de compilación (no debe devolver filas) y estado.
-- ---------------------------------------------------------------------
SELECT NAME, TYPE, LINE, TEXT FROM USER_ERRORS WHERE NAME LIKE 'PKG\_%' ESCAPE '\' ORDER BY NAME, TYPE, SEQUENCE;

SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS WHERE OBJECT_TYPE LIKE 'PACKAGE%' ORDER BY OBJECT_NAME;
