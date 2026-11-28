-- =====================================================================
-- 11_paquete_socios.sql · A4 · PKG_PARTNERS con descripción y foto
--
-- Reemplaza el paquete PKG_PARTNERS de 06b_paquetes_negocio.sql (que no se
-- modifica). Cambios respecto al original:
--   * LIST_ALL, FIND_BY_ID y FIND_BY_TAX_ID devuelven DESCRIPTION e IMAGE_FILE.
--   * INSERT_PARTNER recibe p_description y p_image_file (al final).
--   * UPDATE_PARTNER recibe p_description y p_image_file (antes de p_id).
--
-- Ejecutar DESPUÉS de 09_socios_foto_descripcion.sql (necesita las columnas).
-- Se puede correr varias veces (CREATE OR REPLACE).
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
        p_longitude    IN NUMBER,
        p_description  IN VARCHAR2,
        p_image_file   IN VARCHAR2
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
        p_description  IN VARCHAR2,
        p_image_file   IN VARCHAR2,
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
                   P.DESCRIPTION, P.IMAGE_FILE,
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
                   P.DESCRIPTION, P.IMAGE_FILE,
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
                   P.DESCRIPTION, P.IMAGE_FILE,
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
        p_longitude    IN NUMBER,
        p_description  IN VARCHAR2,
        p_image_file   IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO PARTNERS (PARTNER_TYPE, COMPANY_NAME, TAX_ID, PHONE, STATUS,
                              REGISTRATION_DATE, ADDRESS, LATITUDE, LONGITUDE,
                              DESCRIPTION, IMAGE_FILE)
        VALUES (p_type, p_company_name, p_tax_id, p_phone, p_status,
                p_reg_date, p_address, p_latitude, p_longitude,
                p_description, p_image_file)
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
        p_description  IN VARCHAR2,
        p_image_file   IN VARCHAR2,
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
            LONGITUDE = p_longitude,
            DESCRIPTION = p_description,
            IMAGE_FILE = p_image_file
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
