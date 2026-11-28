-- =====================================================================
-- 10_datos_socios.sql · A4 · Descripciones de los socios de prueba
--
-- Ejecutar después de 09_socios_foto_descripcion.sql. Los socios se unen por
-- NIT, no por ID. Las fotos se suben desde el formulario de socios.
-- =====================================================================

UPDATE PARTNERS SET DESCRIPTION = 'Almacén de repuestos para carro en el centro de Valledupar. Frenos, filtros, bujías y suspensión, con entrega en 2 días.' WHERE TAX_ID = '900123456-1';
UPDATE PARTNERS SET DESCRIPTION = 'Proveedor de repuestos con amplio inventario en Los Mayales. Baterías, filtros y llantas para vehículos livianos.' WHERE TAX_ID = '900234567-2';
UPDATE PARTNERS SET DESCRIPTION = 'Taller especializado en frenos: pastillas, discos y calibración del sistema. Cuenta con 4 bahías de trabajo.' WHERE TAX_ID = '900555444-7';
UPDATE PARTNERS SET DESCRIPTION = 'Taller de llantas, alineación y balanceo en Cañaguate. Atención rápida con 3 bahías de trabajo.' WHERE TAX_ID = '900666555-8';
UPDATE PARTNERS SET DESCRIPTION = 'Importadora de repuestos originales y de marca para carro y camioneta. Retiro en Novalito o envío.' WHERE TAX_ID = '900345678-3';
UPDATE PARTNERS SET DESCRIPTION = 'Especialistas en repuestos para motocicleta: kits de arrastre, bujías, pastillas y llantas.' WHERE TAX_ID = '900456789-4';

COMMIT;

-- Verificación: debe devolver 6 de 6 con descripción
SELECT COUNT(DESCRIPTION) AS CON_DESCRIPCION, COUNT(IMAGE_FILE) AS CON_FOTO, COUNT(*) AS TOTAL FROM PARTNERS;
