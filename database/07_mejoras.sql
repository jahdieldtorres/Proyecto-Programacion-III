-- =====================================================================
-- 07_mejoras.sql · Cambios a la base que ya existe (etapa de mejoras)
--
-- Ejecutar en Database Actions → SQL, después de 01 a 06b.
-- Cada tarea agrega aquí su bloque. Si cambia una tabla, también se
-- actualiza 01_tablas.sql para que el script completo siga sirviendo.
-- =====================================================================


-- ---------------------------------------------------------------------
-- COMPONENTS.DESCRIPTION ya existe (VARCHAR2(300)); el paquete PKG_COMPONENTS
-- (05a) empieza a leerla y guardarla. Descripciones de los repuestos de prueba.
-- ---------------------------------------------------------------------

UPDATE COMPONENTS SET DESCRIPTION = 'Filtro de aceite de flujo estándar. Retiene impurezas y partículas del lubricante para proteger el motor. Para vehículos a gasolina.' WHERE REFERENCE = 'FO-3301';
UPDATE COMPONENTS SET DESCRIPTION = 'Bujía de iridio original de larga duración. Chispa estable, mejor arranque y menor consumo de combustible.' WHERE REFERENCE = 'BI-7742';
UPDATE COMPONENTS SET DESCRIPTION = 'Filtro de aire del motor de papel plisado. Evita que el polvo entre a la admisión y mantiene la mezcla de aire limpia.' WHERE REFERENCE = 'FA-5510';
UPDATE COMPONENTS SET DESCRIPTION = 'Amortiguador delantero hidráulico. Controla el rebote de la suspensión y mejora la estabilidad y el confort al conducir.' WHERE REFERENCE = 'AD-9021';
UPDATE COMPONENTS SET DESCRIPTION = 'Juego de pastillas de freno delanteras de material semimetálico. Buena respuesta de frenado y desgaste uniforme.' WHERE REFERENCE = 'PF-1234';
UPDATE COMPONENTS SET DESCRIPTION = 'Disco de freno ventilado original. Disipa mejor el calor en frenadas fuertes y reduce las vibraciones del pedal.' WHERE REFERENCE = 'DF-4410';
UPDATE COMPONENTS SET DESCRIPTION = 'Batería de 12 V y 45 Ah para vehículos livianos. Libre de mantenimiento y con buen arranque en climas cálidos.' WHERE REFERENCE = 'BT-1245';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Michelin Primacy 4 205/55 R16. Excelente agarre en piso mojado, baja resistencia al rodamiento y larga duración.' WHERE REFERENCE = 'MP4-2055516';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Bridgestone Turanza T005 195/65 R15. Pensada para el confort, el bajo ruido y el control en carretera.' WHERE REFERENCE = 'BT5-1956515';
UPDATE COMPONENTS SET DESCRIPTION = 'Rin de aleación de 17 pulgadas con patrón de 5 tornillos. Más liviano que el de acero y de acabado deportivo.' WHERE REFERENCE = 'RA-17-5114';
UPDATE COMPONENTS SET DESCRIPTION = 'Rin de acero de 15 pulgadas con 4 tornillos. Resistente a golpes y fácil de reparar, ideal para uso diario.' WHERE REFERENCE = 'RA-15-4100';
UPDATE COMPONENTS SET DESCRIPTION = 'Motor 1.6 L de 16 válvulas reacondicionado. Revisado, rectificado y probado antes de la venta.' WHERE REFERENCE = 'MT-16-16V';
UPDATE COMPONENTS SET DESCRIPTION = 'Motor de 150 cc para motocicleta, monocilíndrico de cuatro tiempos. Bajo consumo y fácil mantenimiento.' WHERE REFERENCE = 'MT-150-M';
UPDATE COMPONENTS SET DESCRIPTION = 'Caja automática de 6 velocidades original. Cambios suaves y buena eficiencia de combustible.' WHERE REFERENCE = 'TR-A6-001';
UPDATE COMPONENTS SET DESCRIPTION = 'Caja manual de 5 velocidades. Mecánica robusta y de mantenimiento sencillo.' WHERE REFERENCE = 'TR-M5-002';
UPDATE COMPONENTS SET DESCRIPTION = 'Pastillas de freno traseras originales. Frenado progresivo y silencioso con bajo desgaste del disco.' WHERE REFERENCE = 'PF-1235';
UPDATE COMPONENTS SET DESCRIPTION = 'Pastillas de freno delanteras cerámicas originales. Menos polvo, menos ruido y frenado estable.' WHERE REFERENCE = 'PF-2201';
UPDATE COMPONENTS SET DESCRIPTION = 'Disco de freno trasero sólido original. Superficie rectificada para un frenado parejo.' WHERE REFERENCE = 'DF-4421';
UPDATE COMPONENTS SET DESCRIPTION = 'Pastillas de freno para motocicleta de 150 cc. Material de buena mordida y desgaste controlado.' WHERE REFERENCE = 'PF-M150';
UPDATE COMPONENTS SET DESCRIPTION = 'Bujía NGK BKR6E de níquel original. Encendido confiable en motores de automóvil a gasolina.' WHERE REFERENCE = 'BKR6E';
UPDATE COMPONENTS SET DESCRIPTION = 'Bujía NGK CR8E original para motocicleta. Chispa constante y arranque fácil en motores de baja cilindrada.' WHERE REFERENCE = 'CR8E';
UPDATE COMPONENTS SET DESCRIPTION = 'Filtro del habitáculo que retiene polvo y polen del aire que entra a la cabina. Recomendado cambiarlo cada año.' WHERE REFERENCE = 'FH-6120';
UPDATE COMPONENTS SET DESCRIPTION = 'Correa de accesorios multi-V de 6 canales y 1195 mm. Mueve alternador, bomba de dirección y aire acondicionado.' WHERE REFERENCE = '6PK1195';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Goodyear Assurance 185/65 R15. Buen agarre y durabilidad para uso en ciudad y carretera.' WHERE REFERENCE = 'GY-1856515';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Pirelli Cinturato P1 175/65 R14. Baja resistencia al rodamiento y manejo suave.' WHERE REFERENCE = 'PI-1756514';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Michelin Energy XM2+ 195/55 R16. Ahorra combustible y dura más kilómetros.' WHERE REFERENCE = 'MI-1955516';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Bridgestone Dueler H/T 265/65 R17 para camionetas y SUV. Estable en carretera y ligera fuera de ella.' WHERE REFERENCE = 'BR-2656517';
UPDATE COMPONENTS SET DESCRIPTION = 'Llanta Pirelli Sport Demon 110/70-17 para motocicleta. Agarre deportivo en seco y mojado.' WHERE REFERENCE = 'PI-M1107017';
UPDATE COMPONENTS SET DESCRIPTION = 'Rin de aleación de 15 pulgadas con 4 tornillos. Liviano y de buen acabado.' WHERE REFERENCE = 'RA-15-4101';
UPDATE COMPONENTS SET DESCRIPTION = 'Rin de aleación de 16 pulgadas con 5 tornillos. Liviano, resistente y de diseño deportivo.' WHERE REFERENCE = 'RA-16-5108';
UPDATE COMPONENTS SET DESCRIPTION = 'Rin de acero de 14 pulgadas con 4 tornillos. Económico y resistente para vehículos pequeños.' WHERE REFERENCE = 'RA-14-4100';
UPDATE COMPONENTS SET DESCRIPTION = 'Kit de embrague completo: disco, prensa y balinera. Acople suave y buena duración.' WHERE REFERENCE = 'KE-3001';
UPDATE COMPONENTS SET DESCRIPTION = 'Junta homocinética del lado de la rueda. Transmite la fuerza al eje con giro suave en curvas.' WHERE REFERENCE = 'JH-6401';
UPDATE COMPONENTS SET DESCRIPTION = 'Kit de arrastre 428H para motocicleta: cadena, piñón y catalina. Transmisión resistente al desgaste.' WHERE REFERENCE = 'KA-4281';
UPDATE COMPONENTS SET DESCRIPTION = 'Amortiguador trasero hidráulico. Mejora el confort y mantiene el vehículo estable con carga.' WHERE REFERENCE = 'AT-9130';
UPDATE COMPONENTS SET DESCRIPTION = 'Bombillo halógeno H4 Philips de 12 V y 60/55 W. Luz alta y baja con buena visibilidad nocturna.' WHERE REFERENCE = 'PH-H4-12342';
UPDATE COMPONENTS SET DESCRIPTION = 'Batería Bosch S4 de 12 V y 60 Ah. Alto poder de arranque, sellada y libre de mantenimiento.' WHERE REFERENCE = 'BT-1260';
UPDATE COMPONENTS SET DESCRIPTION = 'Alternador reacondicionado. Carga la batería y alimenta el sistema eléctrico; probado antes de la venta.' WHERE REFERENCE = 'AL-9001';
UPDATE COMPONENTS SET DESCRIPTION = 'Motor de arranque original. Gira el motor con fuerza y de forma confiable en cada encendido.' WHERE REFERENCE = 'MA-8801';
UPDATE COMPONENTS SET DESCRIPTION = 'Bobina de encendido original. Entrega la chispa de alto voltaje a las bujías con un encendido estable.' WHERE REFERENCE = 'BO-4401';
UPDATE COMPONENTS SET DESCRIPTION = 'Semieje de transmisión delantero. Lleva la fuerza de la caja a la rueda con juntas homocinéticas.' WHERE REFERENCE = 'SE-6501';
UPDATE COMPONENTS SET DESCRIPTION = 'Bomba de combustible eléctrica original. Presión estable para una buena alimentación del motor.' WHERE REFERENCE = 'BC-3050';
UPDATE COMPONENTS SET DESCRIPTION = 'Radiador de refrigeración original. Mantiene la temperatura del motor y evita el sobrecalentamiento.' WHERE REFERENCE = 'RC-1100';
UPDATE COMPONENTS SET DESCRIPTION = 'Termostato de motor original. Regula el paso del refrigerante para un calentamiento rápido y parejo.' WHERE REFERENCE = 'TM-2200';
UPDATE COMPONENTS SET DESCRIPTION = 'Cojinete de rueda (balero) original. Permite que la rueda gire sin ruido ni holgura.' WHERE REFERENCE = 'CR-8800';
UPDATE COMPONENTS SET DESCRIPTION = 'Brazo de control de suspensión (tijera). Mantiene la rueda alineada y mejora la dirección.' WHERE REFERENCE = 'BC-9910';
UPDATE COMPONENTS SET DESCRIPTION = 'Caliper de freno delantero original. Aprieta las pastillas contra el disco con presión pareja.' WHERE REFERENCE = 'CF-3320';
UPDATE COMPONENTS SET DESCRIPTION = 'Manguera de freno flexible reforzada. Resiste la presión del líquido y la flexión de la suspensión.' WHERE REFERENCE = 'MF-1140';
UPDATE COMPONENTS SET DESCRIPTION = 'Filtro de aire de motor de alta capacidad. Retiene el polvo y protege los cilindros del desgaste.' WHERE REFERENCE = 'FA-4400';
UPDATE COMPONENTS SET DESCRIPTION = 'Turbocompresor original. Aumenta la potencia del motor y mejora la respuesta a bajas revoluciones.' WHERE REFERENCE = 'TC-7770';

COMMIT;

-- Verificación: debe devolver 50 de 50
SELECT COUNT(DESCRIPTION) AS CON_DESCRIPCION, COUNT(*) AS TOTAL FROM COMPONENTS;
