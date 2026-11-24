-- =====================================================================
-- ORIGEN · Pruebas de los disparadores (opcional, no forma parte de la carga)
-- @author Héctor Guevara
--
-- Cómo correrlo: Database Actions → SQL → pegar todo → "Ejecutar script" (F5),
-- con los datos de 02_datos.sql recién cargados y 03_disparadores.sql ya
-- corrido. Cada prueba termina con ROLLBACK: no deja datos de más.
-- Los mensajes salen en la pestaña "Salida de script".
-- Para las capturas del documento: tomar la salida de cada prueba.
-- =====================================================================
SET SERVEROUTPUT ON;

-- ---------------------------------------------------------------------
-- Prueba 1 · Historial de precios (TRG_INVENTORY_PRICE)
-- Esperado: UNA sola fila en PRICE_HISTORY (185000 → 186000).
-- La segunda actualización deja el mismo precio y no debe registrar nada.
-- ---------------------------------------------------------------------
UPDATE INVENTORY SET PRICE = PRICE + 1000 WHERE ID = 1;
UPDATE INVENTORY SET PRICE = PRICE WHERE ID = 2;

SELECT INVENTORY_ID, OLD_PRICE, NEW_PRICE, CHANGE_DATE FROM PRICE_HISTORY;

ROLLBACK;

-- ---------------------------------------------------------------------
-- Prueba 2 · No borrar repuestos (TRG_COMPONENTS_NO_DELETE)
-- Esperado: "OK" y el error ORA-20003.
-- ---------------------------------------------------------------------
BEGIN
    DELETE FROM COMPONENTS WHERE ID = 1;
    DBMS_OUTPUT.PUT_LINE('FALLÓ LA PRUEBA: el borrado debió dar error.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK, el borrado dio error: ' || SQLERRM);
END;
/

ROLLBACK;

-- ---------------------------------------------------------------------
-- Prueba 3 · Descontar stock y rechazar pedidos de más (TRG_ORDER_ITEMS_STOCK)
-- La oferta 1 tiene 8 unidades con los datos de 02_datos.sql.
-- Esperado: el stock baja 3 y el pedido de 999 unidades da ORA-20001.
-- ---------------------------------------------------------------------
DECLARE
    v_user_id   USERS.ID%TYPE;
    v_order_id  ORDERS.ID%TYPE;
    v_before    INVENTORY.STOCK%TYPE;
    v_after     INVENTORY.STOCK%TYPE;
BEGIN
    SELECT MIN(ID) INTO v_user_id FROM USERS;

    INSERT INTO ORDERS (USER_ID, TOTAL) VALUES (v_user_id, 0)
    RETURNING ID INTO v_order_id;

    SELECT STOCK INTO v_before FROM INVENTORY WHERE ID = 1;

    INSERT INTO ORDER_ITEMS (ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST)
    VALUES (v_order_id, 1, 3, 185000, 12000);

    SELECT STOCK INTO v_after FROM INVENTORY WHERE ID = 1;
    DBMS_OUTPUT.PUT_LINE('Stock antes: ' || v_before || ' · después: ' || v_after
            || ' (debe haber bajado 3)');

    BEGIN
        INSERT INTO ORDER_ITEMS (ORDER_ID, INVENTORY_ID, QUANTITY, UNIT_PRICE, SHIPPING_COST)
        VALUES (v_order_id, 1, 999, 185000, 12000);
        DBMS_OUTPUT.PUT_LINE('FALLÓ LA PRUEBA: pedir 999 unidades debió dar error.');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('OK, pedir de más dio error: ' || SQLERRM);
    END;

    ROLLBACK;
END;
/
