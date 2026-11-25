-- =====================================================================
-- ORIGEN · Script 05b · Paquetes PL/SQL de socios, tarifas, afiliaciones,
--          inventario y pedidos
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
-- @author Héctor Guevara
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5).
-- Se corre DESPUÉS de 01 → 02 → 03 → 04 → 05a. Se puede repetir las veces
-- que haga falta (CREATE OR REPLACE). Si se vuelve a correr 01_tablas.sql,
-- los paquetes quedan inválidos: hay que correr este script otra vez.
--
-- Sigue las mismas reglas de 05a_paquetes_catalogo.sql:
--   1. Un paquete por tabla principal.
--   2. TODO es una FUNCIÓN y Java siempre lee el resultado en el primer ?:
--        - Consultas: devuelven SYS_REFCURSOR (Java lo recibe como ResultSet).
--        - INSERT:    devuelven NUMBER con el ID nuevo (RETURNING ID INTO).
--        - UPDATE:    devuelven NUMBER con las filas cambiadas (SQL%ROWCOUNT).
--                     Si es 0, el registro no existía y el DAO lo informa.
--   3. Ninguna función tiene parámetros OUT: lo que se devuelve es el RETURN.
--   4. Sin COMMIT dentro del paquete: lo confirma Java.
--   5. Sin WHEN OTHERS: los errores tienen que llegar a Java (así funciona
--      el error -20001 de stock del disparador).
--   6. Ninguna función descuenta stock: lo hace el disparador
--      TRG_ORDER_ITEMS_STOCK al insertar cada línea del pedido.
--   7. Los socios, tarifas, afiliaciones y ofertas tienen STATUS: no se
--      borran, se pasan a INACTIVO con UPDATE (los DAO no ofrecen borrar).
--
-- Paquetes de este archivo:
--   PKG_PARTNERS      Socios (proveedores y talleres) y sus subtipos.
--   PKG_FEES          Tarifas de los servicios de los talleres.
--   PKG_AFFILIATIONS  Afiliaciones entre proveedores y talleres.
--   PKG_INVENTORY     Ofertas de inventario de los proveedores.
--   PKG_ORDERS        Pedidos y sus líneas.
--
-- Para comprobar que todo quedó bien:
--   SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS
--   WHERE OBJECT_TYPE LIKE 'PACKAGE%';      -- todos deben decir VALID
-- =====================================================================


-- =====================================================================
-- PKG_PARTNERS · Socios (proveedores y talleres)
-- Lo usa autoparts.dao.oracle.PartnerOracleDAO.
--
-- Cada socio ocupa PARTNERS y, según su tipo, SUPPLIERS o WORKSHOPS con el
-- mismo ID. Igual que PKG_VEHICLES: cada función toca UNA tabla y el DAO las
-- llama en una transacción (setAutoCommit(false), commit y rollback en Java)
-- para que se guarden las dos filas o ninguna.
--
-- En las funciones de los subtipos el ID del socio va de último, así
-- INSERT_xxx y UPDATE_xxx reciben los mismos parámetros.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_PARTNERS AS

    -- Todos los socios con los datos de su subtipo, ordenados por ID.
    -- Los datos del subtipo que no aplica vienen NULL.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- El socio con ese ID, con las mismas columnas (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- El socio con ese NIT, sin importar mayúsculas (cero o una fila).
    FUNCTION FIND_BY_TAX_ID (p_tax_id IN VARCHAR2) RETURN SYS_REFCURSOR;

    -- Guarda la fila de PARTNERS y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_PARTNER (
        p_type         IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_tax_id       IN VARCHAR2,
        p_phone        IN VARCHAR2,
        p_status       IN VARCHAR2,
        p_reg_date     IN DATE,
        p_address      IN VARCHAR2,
        p_latitude     IN NUMBER,
        p_longitude    IN NUMBER
    ) RETURN NUMBER;

    -- Cambia la fila de PARTNERS y devuelve cuántas filas cambió.
    -- El tipo del socio no se cambia nunca.
    FUNCTION UPDATE_PARTNER (
        p_company_name IN VARCHAR2,
        p_tax_id       IN VARCHAR2,
        p_phone        IN VARCHAR2,
        p_status       IN VARCHAR2,
        p_reg_date     IN DATE,
        p_address      IN VARCHAR2,
        p_latitude     IN NUMBER,
        p_longitude    IN NUMBER,
        p_id           IN NUMBER
    ) RETURN NUMBER;

    -- Proveedores
    FUNCTION INSERT_SUPPLIER (
        p_delivery_days IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_SUPPLIER (
        p_delivery_days IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER;

    -- Talleres
    FUNCTION INSERT_WORKSHOP (
        p_specialty IN VARCHAR2, p_bay_count IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER;

    FUNCTION UPDATE_WORKSHOP (
        p_specialty IN VARCHAR2, p_bay_count IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER;

END PKG_PARTNERS;
/

CREATE OR REPLACE PACKAGE BODY PKG_PARTNERS AS

    -- LIST_ALL, FIND_BY_ID y FIND_BY_TAX_ID tienen la misma consulta; solo
    -- cambia el WHERE. Los LEFT JOIN dejan en NULL las columnas del subtipo
    -- que no aplica.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_partners SYS_REFCURSOR;
    BEGIN
        OPEN c_partners FOR
            SELECT P.ID, P.PARTNER_TYPE, P.COMPANY_NAME, P.TAX_ID, P.PHONE, P.STATUS,
                   P.REGISTRATION_DATE, P.ADDRESS, P.LATITUDE, P.LONGITUDE,
                   S.DELIVERY_DAYS, W.SPECIALTY, W.BAY_COUNT
            FROM PARTNERS P
            LEFT JOIN SUPPLIERS S ON S.PARTNER_ID = P.ID
            LEFT JOIN WORKSHOPS W ON W.PARTNER_ID = P.ID
            ORDER BY P.ID;
        RETURN c_partners;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_partner SYS_REFCURSOR;
    BEGIN
        OPEN c_partner FOR
            SELECT P.ID, P.PARTNER_TYPE, P.COMPANY_NAME, P.TAX_ID, P.PHONE, P.STATUS,
                   P.REGISTRATION_DATE, P.ADDRESS, P.LATITUDE, P.LONGITUDE,
                   S.DELIVERY_DAYS, W.SPECIALTY, W.BAY_COUNT
            FROM PARTNERS P
            LEFT JOIN SUPPLIERS S ON S.PARTNER_ID = P.ID
            LEFT JOIN WORKSHOPS W ON W.PARTNER_ID = P.ID
            WHERE P.ID = p_id;
        RETURN c_partner;
    END FIND_BY_ID;

    FUNCTION FIND_BY_TAX_ID (p_tax_id IN VARCHAR2) RETURN SYS_REFCURSOR IS
        c_partner SYS_REFCURSOR;
    BEGIN
        OPEN c_partner FOR
            SELECT P.ID, P.PARTNER_TYPE, P.COMPANY_NAME, P.TAX_ID, P.PHONE, P.STATUS,
                   P.REGISTRATION_DATE, P.ADDRESS, P.LATITUDE, P.LONGITUDE,
                   S.DELIVERY_DAYS, W.SPECIALTY, W.BAY_COUNT
            FROM PARTNERS P
            LEFT JOIN SUPPLIERS S ON S.PARTNER_ID = P.ID
            LEFT JOIN WORKSHOPS W ON W.PARTNER_ID = P.ID
            WHERE UPPER(P.TAX_ID) = UPPER(p_tax_id);
        RETURN c_partner;
    END FIND_BY_TAX_ID;

    FUNCTION INSERT_PARTNER (
        p_type         IN VARCHAR2,
        p_company_name IN VARCHAR2,
        p_tax_id       IN VARCHAR2,
        p_phone        IN VARCHAR2,
        p_status       IN VARCHAR2,
        p_reg_date     IN DATE,
        p_address      IN VARCHAR2,
        p_latitude     IN NUMBER,
        p_longitude    IN NUMBER
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO PARTNERS (PARTNER_TYPE, COMPANY_NAME, TAX_ID, PHONE, STATUS,
                              REGISTRATION_DATE, ADDRESS, LATITUDE, LONGITUDE)
        VALUES (p_type, p_company_name, p_tax_id, p_phone, p_status,
                p_reg_date, p_address, p_latitude, p_longitude)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_PARTNER;

    FUNCTION UPDATE_PARTNER (
        p_company_name IN VARCHAR2,
        p_tax_id       IN VARCHAR2,
        p_phone        IN VARCHAR2,
        p_status       IN VARCHAR2,
        p_reg_date     IN DATE,
        p_address      IN VARCHAR2,
        p_latitude     IN NUMBER,
        p_longitude    IN NUMBER,
        p_id           IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE PARTNERS
        SET COMPANY_NAME = p_company_name,
            TAX_ID = p_tax_id,
            PHONE = p_phone,
            STATUS = p_status,
            REGISTRATION_DATE = p_reg_date,
            ADDRESS = p_address,
            LATITUDE = p_latitude,
            LONGITUDE = p_longitude
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_PARTNER;

    FUNCTION INSERT_SUPPLIER (
        p_delivery_days IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO SUPPLIERS (DELIVERY_DAYS, PARTNER_ID)
        VALUES (p_delivery_days, p_partner_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_SUPPLIER;

    FUNCTION UPDATE_SUPPLIER (
        p_delivery_days IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE SUPPLIERS
        SET DELIVERY_DAYS = p_delivery_days
        WHERE PARTNER_ID = p_partner_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_SUPPLIER;

    FUNCTION INSERT_WORKSHOP (
        p_specialty IN VARCHAR2, p_bay_count IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        INSERT INTO WORKSHOPS (SPECIALTY, BAY_COUNT, PARTNER_ID)
        VALUES (p_specialty, p_bay_count, p_partner_id);
        RETURN SQL%ROWCOUNT;
    END INSERT_WORKSHOP;

    FUNCTION UPDATE_WORKSHOP (
        p_specialty IN VARCHAR2, p_bay_count IN NUMBER, p_partner_id IN NUMBER
    ) RETURN NUMBER IS
    BEGIN
        UPDATE WORKSHOPS
        SET SPECIALTY = p_specialty,
            BAY_COUNT = p_bay_count
        WHERE PARTNER_ID = p_partner_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_WORKSHOP;

END PKG_PARTNERS;
/


-- =====================================================================
-- PKG_FEES · Tarifas de los servicios de los talleres
-- Lo usa autoparts.dao.oracle.FeeOracleDAO.
-- Las tarifas no se borran: se pasan a INACTIVO con UPDATE_FEE.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_FEES AS

    -- Todas las tarifas, ordenadas por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- La tarifa con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las tarifas de un servicio, ordenadas por ID.
    FUNCTION LIST_BY_SERVICE (p_service_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una tarifa nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_FEE (
        p_service_id IN NUMBER,
        p_amount     IN NUMBER,
        p_start_date IN DATE,
        p_status     IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia los datos de una tarifa y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID).
    FUNCTION UPDATE_FEE (
        p_id         IN NUMBER,
        p_service_id IN NUMBER,
        p_amount     IN NUMBER,
        p_start_date IN DATE,
        p_status     IN VARCHAR2
    ) RETURN NUMBER;

END PKG_FEES;
/

CREATE OR REPLACE PACKAGE BODY PKG_FEES AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_fees SYS_REFCURSOR;
    BEGIN
        OPEN c_fees FOR
            SELECT ID, SERVICE_ID, AMOUNT, START_DATE, STATUS
            FROM FEES
            ORDER BY ID;
        RETURN c_fees;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_fee SYS_REFCURSOR;
    BEGIN
        OPEN c_fee FOR
            SELECT ID, SERVICE_ID, AMOUNT, START_DATE, STATUS
            FROM FEES
            WHERE ID = p_id;
        RETURN c_fee;
    END FIND_BY_ID;

    FUNCTION LIST_BY_SERVICE (p_service_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_fees SYS_REFCURSOR;
    BEGIN
        OPEN c_fees FOR
            SELECT ID, SERVICE_ID, AMOUNT, START_DATE, STATUS
            FROM FEES
            WHERE SERVICE_ID = p_service_id
            ORDER BY ID;
        RETURN c_fees;
    END LIST_BY_SERVICE;

    FUNCTION INSERT_FEE (
        p_service_id IN NUMBER,
        p_amount     IN NUMBER,
        p_start_date IN DATE,
        p_status     IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO FEES (SERVICE_ID, AMOUNT, START_DATE, STATUS)
        VALUES (p_service_id, p_amount, p_start_date, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_FEE;

    FUNCTION UPDATE_FEE (
        p_id         IN NUMBER,
        p_service_id IN NUMBER,
        p_amount     IN NUMBER,
        p_start_date IN DATE,
        p_status     IN VARCHAR2
    ) RETURN NUMBER IS
    BEGIN
        UPDATE FEES
        SET SERVICE_ID = p_service_id,
            AMOUNT = p_amount,
            START_DATE = p_start_date,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_FEE;

END PKG_FEES;
/


-- =====================================================================
-- PKG_AFFILIATIONS · Afiliaciones entre proveedores y talleres
-- Lo usa autoparts.dao.oracle.AffiliationOracleDAO.
-- Las afiliaciones no se borran: se pasan a INACTIVO con UPDATE_AFFILIATION.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_AFFILIATIONS AS

    -- Todas las afiliaciones, ordenadas por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- La afiliación con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las afiliaciones de un taller, ordenadas por ID.
    FUNCTION LIST_BY_WORKSHOP (p_workshop_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las afiliaciones de un proveedor, ordenadas por ID.
    FUNCTION LIST_BY_SUPPLIER (p_supplier_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una afiliación nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_AFFILIATION (
        p_supplier_id IN NUMBER,
        p_workshop_id IN NUMBER,
        p_start_date  IN DATE,
        p_status      IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia los datos de una afiliación y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID).
    FUNCTION UPDATE_AFFILIATION (
        p_id          IN NUMBER,
        p_supplier_id IN NUMBER,
        p_workshop_id IN NUMBER,
        p_start_date  IN DATE,
        p_status      IN VARCHAR2
    ) RETURN NUMBER;

END PKG_AFFILIATIONS;
/

CREATE OR REPLACE PACKAGE BODY PKG_AFFILIATIONS AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_affiliations SYS_REFCURSOR;
    BEGIN
        OPEN c_affiliations FOR
            SELECT ID, SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS
            FROM AFFILIATIONS
            ORDER BY ID;
        RETURN c_affiliations;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_affiliation SYS_REFCURSOR;
    BEGIN
        OPEN c_affiliation FOR
            SELECT ID, SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS
            FROM AFFILIATIONS
            WHERE ID = p_id;
        RETURN c_affiliation;
    END FIND_BY_ID;

    FUNCTION LIST_BY_WORKSHOP (p_workshop_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_affiliations SYS_REFCURSOR;
    BEGIN
        OPEN c_affiliations FOR
            SELECT ID, SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS
            FROM AFFILIATIONS
            WHERE WORKSHOP_ID = p_workshop_id
            ORDER BY ID;
        RETURN c_affiliations;
    END LIST_BY_WORKSHOP;

    FUNCTION LIST_BY_SUPPLIER (p_supplier_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_affiliations SYS_REFCURSOR;
    BEGIN
        OPEN c_affiliations FOR
            SELECT ID, SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS
            FROM AFFILIATIONS
            WHERE SUPPLIER_ID = p_supplier_id
            ORDER BY ID;
        RETURN c_affiliations;
    END LIST_BY_SUPPLIER;

    FUNCTION INSERT_AFFILIATION (
        p_supplier_id IN NUMBER,
        p_workshop_id IN NUMBER,
        p_start_date  IN DATE,
        p_status      IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO AFFILIATIONS (SUPPLIER_ID, WORKSHOP_ID, START_DATE, STATUS)
        VALUES (p_supplier_id, p_workshop_id, p_start_date, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_AFFILIATION;

    FUNCTION UPDATE_AFFILIATION (
        p_id          IN NUMBER,
        p_supplier_id IN NUMBER,
        p_workshop_id IN NUMBER,
        p_start_date  IN DATE,
        p_status      IN VARCHAR2
    ) RETURN NUMBER IS
    BEGIN
        UPDATE AFFILIATIONS
        SET SUPPLIER_ID = p_supplier_id,
            WORKSHOP_ID = p_workshop_id,
            START_DATE = p_start_date,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_AFFILIATION;

END PKG_AFFILIATIONS;
/


-- =====================================================================
-- PKG_INVENTORY · Ofertas de inventario de los proveedores
-- Lo usa autoparts.dao.oracle.InventoryOracleDAO.
--
-- Esta función no descuenta el stock de los pedidos (lo hace
-- TRG_ORDER_ITEMS_STOCK) ni guarda el historial de precios (lo hace
-- TRG_INVENTORY_PRICE al cambiar PRICE en UPDATE_INVENTORY).
-- Las ofertas no se borran: se pasan a INACTIVO con UPDATE_INVENTORY.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_INVENTORY AS

    -- Todas las ofertas, ordenadas por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- La oferta con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las ofertas de un proveedor, ordenadas por ID.
    FUNCTION LIST_BY_SUPPLIER (p_supplier_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las ofertas de un componente, ordenadas por ID.
    FUNCTION LIST_BY_COMPONENT (p_component_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una oferta nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_INVENTORY (
        p_supplier_id     IN NUMBER,
        p_component_id    IN NUMBER,
        p_price           IN NUMBER,
        p_stock           IN NUMBER,
        p_shipping_cost   IN NUMBER,
        p_warranty_months IN NUMBER,
        p_condition       IN VARCHAR2,
        p_description     IN VARCHAR2,
        p_status          IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia los datos de una oferta y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID).
    FUNCTION UPDATE_INVENTORY (
        p_id              IN NUMBER,
        p_supplier_id     IN NUMBER,
        p_component_id    IN NUMBER,
        p_price           IN NUMBER,
        p_stock           IN NUMBER,
        p_shipping_cost   IN NUMBER,
        p_warranty_months IN NUMBER,
        p_condition       IN VARCHAR2,
        p_description     IN VARCHAR2,
        p_status          IN VARCHAR2
    ) RETURN NUMBER;

END PKG_INVENTORY;
/

CREATE OR REPLACE PACKAGE BODY PKG_INVENTORY AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_items SYS_REFCURSOR;
    BEGIN
        OPEN c_items FOR
            SELECT ID, SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST,
                   WARRANTY_MONTHS, CONDITION, DESCRIPTION, STATUS
            FROM INVENTORY
            ORDER BY ID;
        RETURN c_items;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_item SYS_REFCURSOR;
    BEGIN
        OPEN c_item FOR
            SELECT ID, SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST,
                   WARRANTY_MONTHS, CONDITION, DESCRIPTION, STATUS
            FROM INVENTORY
            WHERE ID = p_id;
        RETURN c_item;
    END FIND_BY_ID;

    FUNCTION LIST_BY_SUPPLIER (p_supplier_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_items SYS_REFCURSOR;
    BEGIN
        OPEN c_items FOR
            SELECT ID, SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST,
                   WARRANTY_MONTHS, CONDITION, DESCRIPTION, STATUS
            FROM INVENTORY
            WHERE SUPPLIER_ID = p_supplier_id
            ORDER BY ID;
        RETURN c_items;
    END LIST_BY_SUPPLIER;

    FUNCTION LIST_BY_COMPONENT (p_component_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_items SYS_REFCURSOR;
    BEGIN
        OPEN c_items FOR
            SELECT ID, SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST,
                   WARRANTY_MONTHS, CONDITION, DESCRIPTION, STATUS
            FROM INVENTORY
            WHERE COMPONENT_ID = p_component_id
            ORDER BY ID;
        RETURN c_items;
    END LIST_BY_COMPONENT;

    FUNCTION INSERT_INVENTORY (
        p_supplier_id     IN NUMBER,
        p_component_id    IN NUMBER,
        p_price           IN NUMBER,
        p_stock           IN NUMBER,
        p_shipping_cost   IN NUMBER,
        p_warranty_months IN NUMBER,
        p_condition       IN VARCHAR2,
        p_description     IN VARCHAR2,
        p_status          IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO INVENTORY (SUPPLIER_ID, COMPONENT_ID, PRICE, STOCK, SHIPPING_COST,
                               WARRANTY_MONTHS, CONDITION, DESCRIPTION, STATUS)
        VALUES (p_supplier_id, p_component_id, p_price, p_stock, p_shipping_cost,
                p_warranty_months, p_condition, p_description, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_INVENTORY;

    FUNCTION UPDATE_INVENTORY (
        p_id              IN NUMBER,
        p_supplier_id     IN NUMBER,
        p_component_id    IN NUMBER,
        p_price           IN NUMBER,
        p_stock           IN NUMBER,
        p_shipping_cost   IN NUMBER,
        p_warranty_months IN NUMBER,
        p_condition       IN VARCHAR2,
        p_description     IN VARCHAR2,
        p_status          IN VARCHAR2
    ) RETURN NUMBER IS
    BEGIN
        UPDATE INVENTORY
        SET SUPPLIER_ID = p_supplier_id,
            COMPONENT_ID = p_component_id,
            PRICE = p_price,
            STOCK = p_stock,
            SHIPPING_COST = p_shipping_cost,
            WARRANTY_MONTHS = p_warranty_months,
            CONDITION = p_condition,
            DESCRIPTION = p_description,
            STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_INVENTORY;

END PKG_INVENTORY;
/


-- =====================================================================
-- PKG_ORDERS · Pedidos y sus líneas
-- Lo usa autoparts.dao.oracle.OrderOracleDAO.
--
-- Cada pedido ocupa ORDERS y una fila de ORDER_ITEMS por línea. Igual que
-- PKG_COMPONENTS: cada función toca UNA tabla y el DAO las llama en una
-- transacción (commit y rollback en Java) para que se guarden el pedido y
-- todas sus líneas, o nada.
--
-- INSERT_ORDER_ITEM dispara TRG_ORDER_ITEMS_STOCK: si no alcanza el stock,
-- el disparador responde con el error -20001, que llega a Java sin
-- cambios (no hay WHEN OTHERS).
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_ORDERS AS

    -- El pedido con ese ID, sin sus líneas (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Los pedidos de un usuario, del más reciente al más antiguo, sin sus
    -- líneas.
    FUNCTION LIST_BY_USER (p_user_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las líneas de un pedido, ordenadas por ID.
    FUNCTION LIST_ITEMS (p_order_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda la fila de ORDERS y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_ORDER (
        p_user_id    IN NUMBER,
        p_order_date IN DATE,
        p_status     IN VARCHAR2,
        p_total      IN NUMBER
    ) RETURN NUMBER;

    -- Guarda una línea del pedido y devuelve el ID que le dio Oracle.
    -- Aquí el disparador revisa y resta el stock.
    FUNCTION INSERT_ORDER_ITEM (
        p_order_id      IN NUMBER,
        p_inventory_id  IN NUMBER,
        p_quantity      IN NUMBER,
        p_unit_price    IN NUMBER,
        p_shipping_cost IN NUMBER
    ) RETURN NUMBER;

END PKG_ORDERS;
/

CREATE OR REPLACE PACKAGE BODY PKG_ORDERS AS

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_order SYS_REFCURSOR;
    BEGIN
        OPEN c_order FOR
            SELECT ID, USER_ID, ORDER_DATE, STATUS
            FROM ORDERS
            WHERE ID = p_id;
        RETURN c_order;
    END FIND_BY_ID;

    FUNCTION LIST_BY_USER (p_user_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_orders SYS_REFCURSOR;
    BEGIN
        OPEN c_orders FOR
            SELECT ID, USER_ID, ORDER_DATE, STATUS
            FROM ORDERS
            WHERE USER_ID = p_user_id
            ORDER BY ID DESC;
        RETURN c_orders;
    END LIST_BY_USER;

    FUNCTION LIST_ITEMS (p_order_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_items SYS_REFCURSOR;
    BEGIN
        OPEN c_items FOR
            SELECT ID, ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST
            FROM ORDER_ITEMS
            WHERE ORDER_ID = p_order_id
            ORDER BY ID;
        RETURN c_items;
    END LIST_ITEMS;

    FUNCTION INSERT_ORDER (
        p_user_id    IN NUMBER,
        p_order_date IN DATE,
        p_status     IN VARCHAR2,
        p_total      IN NUMBER
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO ORDERS (USER_ID, ORDER_DATE, STATUS, TOTAL)
        VALUES (p_user_id, p_order_date, p_status, p_total)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_ORDER;

    FUNCTION INSERT_ORDER_ITEM (
        p_order_id      IN NUMBER,
        p_inventory_id  IN NUMBER,
        p_quantity      IN NUMBER,
        p_unit_price    IN NUMBER,
        p_shipping_cost IN NUMBER
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO ORDER_ITEMS (ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST)
        VALUES (p_order_id, p_inventory_id, p_quantity, p_unit_price, p_shipping_cost)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_ORDER_ITEM;

END PKG_ORDERS;
/

-- ---------------------------------------------------------------------
-- Revisión: errores de compilación (no debe devolver filas) y estado.
-- ---------------------------------------------------------------------
SELECT NAME, TYPE, LINE, TEXT FROM USER_ERRORS WHERE NAME LIKE 'PKG\_%' ESCAPE '\' ORDER BY NAME, TYPE, SEQUENCE;

SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS WHERE OBJECT_TYPE LIKE 'PACKAGE%' ORDER BY OBJECT_NAME;
