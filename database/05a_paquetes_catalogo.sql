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
--   7. "Eliminar" es poner STATUS = 'INACTIVO', no DELETE.
--
-- Paquetes de este archivo:
--   PKG_USERS             Usuarios (ejemplo de referencia para los demás).
--   PKG_INVENTORY_IMAGES  Fotos de cada oferta de inventario.
--   (aquí se agregan PKG_BRANDS, PKG_CATEGORIES, PKG_COMPONENTS,
--    PKG_VEHICLES y PKG_COMPATIBILITY)
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

-- ---------------------------------------------------------------------
-- Revisión: errores de compilación (no debe devolver filas) y estado.
-- ---------------------------------------------------------------------
SELECT NAME, TYPE, LINE, TEXT FROM USER_ERRORS WHERE NAME LIKE 'PKG\_%' ESCAPE '\' ORDER BY NAME, TYPE, SEQUENCE;

SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS WHERE OBJECT_TYPE LIKE 'PACKAGE%' ORDER BY OBJECT_NAME;
