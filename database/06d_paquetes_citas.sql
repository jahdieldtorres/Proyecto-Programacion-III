-- =====================================================================
-- ORIGEN · Script 06d · Paquete PL/SQL de la agenda de citas
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
-- @author Héctor Guevara
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5).
-- Se corre DESPUÉS de crear la tabla APPOINTMENTS (07_mejoras.sql o
-- 01_tablas.sql) y de 06c. Se puede repetir las veces que haga falta
-- (CREATE OR REPLACE).
--
-- Sigue las mismas reglas de 05a y 06b: todo es una FUNCIÓN, las consultas
-- devuelven SYS_REFCURSOR, INSERT devuelve el ID nuevo, UPDATE devuelve las
-- filas cambiadas, sin COMMIT y sin WHEN OTHERS.
--
-- Las citas no se borran: se pasan a CANCELADA con UPDATE_STATUS.
--
-- Para comprobar que quedó bien:
--   SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM USER_OBJECTS
--   WHERE OBJECT_NAME = 'PKG_APPOINTMENTS';  -- los dos deben decir VALID
-- =====================================================================


-- =====================================================================
-- PKG_APPOINTMENTS · Citas de los clientes con los talleres
-- Lo usa autoparts.dao.oracle.AppointmentOracleDAO.
-- =====================================================================

CREATE OR REPLACE PACKAGE PKG_APPOINTMENTS AS

    -- La cita con ese ID (cero o una fila).
    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Las citas de un cliente, de la más próxima a la más antigua.
    FUNCTION LIST_BY_USER (p_user_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Guarda una cita nueva y devuelve el ID que le dio Oracle.
    FUNCTION INSERT_APPOINTMENT (
        p_user_id          IN NUMBER,
        p_service_id       IN NUMBER,
        p_appointment_date IN DATE,
        p_status           IN VARCHAR2
    ) RETURN NUMBER;

    -- Cambia el estado de una cita y devuelve cuántas filas cambió
    -- (1 si existía, 0 si no existe ese ID).
    FUNCTION UPDATE_STATUS (
        p_id     IN NUMBER,
        p_status IN VARCHAR2
    ) RETURN NUMBER;

END PKG_APPOINTMENTS;
/

CREATE OR REPLACE PACKAGE BODY PKG_APPOINTMENTS AS

    FUNCTION FIND_BY_ID (p_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_appointment SYS_REFCURSOR;
    BEGIN
        OPEN c_appointment FOR
            SELECT ID, USER_ID, SERVICE_ID, APPOINTMENT_DATE, STATUS
            FROM APPOINTMENTS
            WHERE ID = p_id;
        RETURN c_appointment;
    END FIND_BY_ID;

    FUNCTION LIST_BY_USER (p_user_id IN NUMBER) RETURN SYS_REFCURSOR IS
        c_appointments SYS_REFCURSOR;
    BEGIN
        OPEN c_appointments FOR
            SELECT ID, USER_ID, SERVICE_ID, APPOINTMENT_DATE, STATUS
            FROM APPOINTMENTS
            WHERE USER_ID = p_user_id
            ORDER BY APPOINTMENT_DATE DESC, ID DESC;
        RETURN c_appointments;
    END LIST_BY_USER;

    FUNCTION INSERT_APPOINTMENT (
        p_user_id          IN NUMBER,
        p_service_id       IN NUMBER,
        p_appointment_date IN DATE,
        p_status           IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        INSERT INTO APPOINTMENTS (USER_ID, SERVICE_ID, APPOINTMENT_DATE, STATUS)
        VALUES (p_user_id, p_service_id, p_appointment_date, p_status)
        RETURNING ID INTO v_id;
        RETURN v_id;
    END INSERT_APPOINTMENT;

    FUNCTION UPDATE_STATUS (
        p_id     IN NUMBER,
        p_status IN VARCHAR2
    ) RETURN NUMBER IS
    BEGIN
        UPDATE APPOINTMENTS
        SET STATUS = p_status
        WHERE ID = p_id;
        RETURN SQL%ROWCOUNT;
    END UPDATE_STATUS;

END PKG_APPOINTMENTS;
/
