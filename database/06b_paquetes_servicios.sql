-- =====================================================================
-- ORIGEN · Script 06c · Paquete PL/SQL de los servicios de los talleres
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
-- @author Héctor Guevara
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5).
-- Se corre DESPUÉS de 06b. Se puede repetir las veces que haga falta
-- (CREATE OR REPLACE). Si se vuelve a correr 01_tablas.sql, el paquete queda
-- inválido: hay que correr este script otra vez.
--
-- Sigue las mismas reglas de 05a y 06b: todo es una FUNCIÓN, las consultas
-- devuelven SYS_REFCURSOR, sin COMMIT y sin WHEN OTHERS.
--
-- Este paquete solo CONSULTA: los servicios de los talleres ya existen en
-- la tabla SERVICES (datos de 02_datos.sql) y las tarifas se leen con
-- PKG_FEES. Para crear o editar servicios se agregarán funciones aquí.
--
-- Para comprobar que quedó bien:
--   SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS
--   WHERE OBJECT_NAME = 'PKG_SERVICES';      -- los dos deben decir VALID
-- =====================================================================


-- =====================================================================
-- PKG_SERVICES · Servicios que ofrecen los talleres
-- Lo usa autoparts.dao.oracle.WorkshopServiceOracleDAO.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_SERVICES AS

    -- Todos los servicios, ordenados por ID.
    FUNCTION LIST_ALL RETURN SYS_REFCURSOR;

    -- El servicio con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Los servicios de un taller, ordenados por ID.
    FUNCTION LIST_BY_WORKSHOP (p_workshop_id IN NUMBER) RETURN SYS_REFCURSOR;

END PKG_SERVICES;
/

CREATE OR REPLACE PACKAGE BODY PKG_SERVICES AS

    FUNCTION LIST_ALL RETURN SYS_REFCURSOR IS
        c_services SYS_REFCURSOR;
    BEGIN
        OPEN c_services FOR
            SELECT ID, WORKSHOP_ID, NAME, DESCRIPTION, STATUS
            FROM SERVICES
            ORDER BY ID;
        RETURN c_services;
    END LIST_ALL;

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_service SYS_REFCURSOR;
    BEGIN
        OPEN c_service FOR
            SELECT ID, WORKSHOP_ID, NAME, DESCRIPTION, STATUS
            FROM SERVICES
            WHERE ID = p_id;
        RETURN c_service;
    END FIND_BY_ID;

    FUNCTION LIST_BY_WORKSHOP (p_workshop_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_services SYS_REFCURSOR;
    BEGIN
        OPEN c_services FOR
            SELECT ID, WORKSHOP_ID, NAME, DESCRIPTION, STATUS
            FROM SERVICES
            WHERE WORKSHOP_ID = p_workshop_id
            ORDER BY ID;
        RETURN c_services;
    END LIST_BY_WORKSHOP;

END PKG_SERVICES;
/
