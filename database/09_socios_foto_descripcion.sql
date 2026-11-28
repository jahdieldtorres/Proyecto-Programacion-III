-- =====================================================================
-- 09_socios_foto_descripcion.sql · A4 · Foto y descripción de los socios
--
-- Agrega PARTNERS.DESCRIPTION y PARTNERS.IMAGE_FILE. Las fotos se guardan
-- en images/partners/ (en la base solo va el nombre del archivo).
--
-- Solo estructura (01_tablas.sql no se modifica). Orden de ejecución en
-- Database Actions → SQL:
--   1) Este archivo (09): agrega las columnas.
--   2) 10_datos_socios.sql: descripciones de los socios de prueba.
--   3) 11_paquete_socios.sql: recrea PKG_PARTNERS con las columnas nuevas.
--
-- Se puede correr varias veces: si una columna ya existe, no hace nada.
-- =====================================================================

DECLARE
    PROCEDURE add_column(p_name IN VARCHAR2, p_definition IN VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'PARTNERS' AND COLUMN_NAME = p_name;
        IF v_count = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE PARTNERS ADD (' || p_name || ' ' || p_definition || ')';
        END IF;
    END add_column;
BEGIN
    add_column('DESCRIPTION', 'VARCHAR2(300)');
    add_column('IMAGE_FILE',  'VARCHAR2(120)');
END;
/
