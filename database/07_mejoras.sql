-- =====================================================================
-- ORIGEN · Script 07 · Mejoras después de la entrega final
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
--
-- Aquí van los cambios a la base que YA EXISTE (ALTER TABLE, CREATE TABLE
-- nuevas, UPDATE de datos de prueba). Cada cambio también se copia en
-- 01_tablas.sql, para que el script completo siga sirviendo.
--
-- Cómo correrlo: Database Actions → SQL → pegar el bloque nuevo → F5.
-- Cada integrante agrega su bloque con su encabezado; si el archivo ya
-- existe en develop, se agrega el bloque al final y se conservan los demás.
-- =====================================================================


-- ---------------------------------------------------------------------
-- H5 · Agenda de citas (Héctor Guevara)
-- Tabla de citas de los clientes con los servicios de los talleres.
-- Después de crearla, correr 06d_paquetes_citas.sql.
-- ---------------------------------------------------------------------
CREATE TABLE APPOINTMENTS (
    ID                NUMBER GENERATED ALWAYS AS IDENTITY,
    USER_ID           NUMBER NOT NULL,
    SERVICE_ID        NUMBER NOT NULL,
    APPOINTMENT_DATE  DATE NOT NULL,                 -- fecha y hora de la cita
    STATUS            VARCHAR2(12) DEFAULT 'PENDIENTE' NOT NULL,
    CONSTRAINT PK_APPOINTMENTS PRIMARY KEY (ID),
    CONSTRAINT FK_APPOINTMENTS_USER FOREIGN KEY (USER_ID) REFERENCES USERS (ID),
    CONSTRAINT FK_APPOINTMENTS_SERVICE FOREIGN KEY (SERVICE_ID) REFERENCES SERVICES (ID),
    CONSTRAINT CK_APPOINTMENTS_STATUS CHECK (STATUS IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA'))
);

CREATE INDEX IX_APPOINTMENTS_USER     ON APPOINTMENTS (USER_ID);
CREATE INDEX IX_APPOINTMENTS_SERVICE  ON APPOINTMENTS (SERVICE_ID);
