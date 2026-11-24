-- =====================================================================
-- ORIGEN · Script 03 · Disparadores
-- Programación de Computadores III · Universidad Popular del Cesar
-- Motor: Oracle Autonomous Database (Always Free)
-- @author Héctor Guevara
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5).
-- Se corre DESPUÉS de 01_tablas.sql y 02_datos.sql. Se puede repetir las
-- veces que haga falta (CREATE OR REPLACE). Si se vuelve a correr 01_tablas.sql
-- los disparadores se borran junto con las tablas: hay que correr este
-- script otra vez.
--
-- Son tres disparadores. Las validaciones normales siguen en los servicios
-- de Java; los disparadores son una protección extra de la base:
--
--   TRG_ORDER_ITEMS_STOCK     ORDER_ITEMS  Revisa el stock y lo descuenta.
--   TRG_INVENTORY_PRICE       INVENTORY    Guarda el historial de precios.
--   TRG_COMPONENTS_NO_DELETE  COMPONENTS   Impide borrar repuestos.
--
-- Códigos de error que reciben los DAO de Java (SQLException.getErrorCode()
-- devuelve el número sin el signo menos del ORA-):
--   -20001  No hay stock suficiente.
--   -20002  La oferta (INVENTORY) no existe.
--   -20003  Los repuestos no se borran, se desactivan.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Descontar el stock al insertar en ORDER_ITEMS
--
--    El stock se descuenta SOLO aquí. OrderService en Java no lo resta,
--    para no descontarlo dos veces.
--    FOR UPDATE bloquea la fila de INVENTORY hasta que termine la
--    transacción, así dos pedidos al mismo tiempo no pueden gastar el
--    mismo stock.
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ORDER_ITEMS_STOCK
BEFORE INSERT ON ORDER_ITEMS
FOR EACH ROW
DECLARE
    v_stock INVENTORY.STOCK%TYPE;
BEGIN
    SELECT STOCK INTO v_stock
    FROM INVENTORY
    WHERE ID = :NEW.INVENTORY_ID
    FOR UPDATE;

    IF v_stock < :NEW.QUANTITY THEN
        RAISE_APPLICATION_ERROR(-20001, 'No hay stock suficiente.');
    END IF;

    UPDATE INVENTORY
    SET STOCK = STOCK - :NEW.QUANTITY
    WHERE ID = :NEW.INVENTORY_ID;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20002, 'La oferta no existe.');
END;
/

-- ---------------------------------------------------------------------
-- 2. Guardar el historial de precios al cambiar INVENTORY.PRICE
--
--    La cláusula WHEN es necesaria: InventoryOracleDAO.update() envía todas
--    las columnas en cada actualización (también PRICE). Sin el WHEN se
--    guardaría un registro cada vez que alguien edite el stock o la garantía
--    aunque el precio no haya cambiado.
--    CHANGE_DATE y el ID los llena la base solos.
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_INVENTORY_PRICE
AFTER UPDATE OF PRICE ON INVENTORY
FOR EACH ROW
WHEN (NEW.PRICE <> OLD.PRICE)
BEGIN
    INSERT INTO PRICE_HISTORY (INVENTORY_ID, OLD_PRICE, NEW_PRICE)
    VALUES (:OLD.ID, :OLD.PRICE, :NEW.PRICE);
END;
/

-- ---------------------------------------------------------------------
-- 3. Impedir borrar repuestos de COMPONENTS
--
--    Los repuestos se desactivan (STATUS = 'INACTIVO'), nunca se borran,
--    para no perder el historial de ofertas, pedidos y compatibilidad.
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_COMPONENTS_NO_DELETE
BEFORE DELETE ON COMPONENTS
FOR EACH ROW
BEGIN
    RAISE_APPLICATION_ERROR(-20003, 'Los repuestos no se borran, se desactivan.');
END;
/

-- ---------------------------------------------------------------------
-- Revisión
-- Primero: errores de compilación. Si todo está bien, esta consulta no
-- devuelve filas.
-- Segundo: los tres disparadores deben aparecer con estado ENABLED.
-- ---------------------------------------------------------------------
SELECT NAME, LINE, TEXT FROM USER_ERRORS WHERE TYPE = 'TRIGGER' ORDER BY NAME, SEQUENCE;

SELECT TRIGGER_NAME, TABLE_NAME, TRIGGERING_EVENT, STATUS
FROM USER_TRIGGERS
WHERE TRIGGER_NAME IN ('TRG_ORDER_ITEMS_STOCK', 'TRG_INVENTORY_PRICE', 'TRG_COMPONENTS_NO_DELETE')
ORDER BY TRIGGER_NAME;
