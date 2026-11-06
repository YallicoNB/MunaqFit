-- Verificacion segura para el error:
-- "Found non-empty schema(s) `munaqfit` but no schema history table".
--
-- Este archivo es solo de lectura: NO crea ni modifica tablas o datos.
-- El baseline lo registra Flyway al iniciar el backend; no se debe insertar
-- manualmente en flyway_schema_history porque Flyway administra su estructura.
--
-- Antes de habilitar un baseline, respalda la base y confirma que las tablas
-- actuales corresponden a backend/src/main/resources/db/migration/V1__baseline.sql.
-- No ejecutes database/schema.sql para resolver este error: ese archivo borra
-- y vuelve a crear la base de datos.

USE munaqfit;

-- Tablas que existen actualmente en la base.
SELECT
  TABLE_NAME AS tabla
FROM information_schema.tables
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- Indica si Flyway ya tiene su tabla de historial.
SELECT
  CASE
    WHEN COUNT(*) > 0 THEN 'Existe flyway_schema_history'
    ELSE 'No existe flyway_schema_history'
  END AS estado_historial_flyway
FROM information_schema.tables
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'flyway_schema_history';

-- Comprueba algunas tablas esperadas por el esquema actual.
SELECT
  requeridas.tabla,
  CASE
    WHEN existentes.TABLE_NAME IS NULL THEN 'FALTA'
    ELSE 'OK'
  END AS estado
FROM (
  SELECT 'usuario' AS tabla
  UNION ALL SELECT 'producto'
  UNION ALL SELECT 'producto_proveedor'
  UNION ALL SELECT 'bebida'
  UNION ALL SELECT 'movimiento_inventario'
  UNION ALL SELECT 'venta'
) AS requeridas
LEFT JOIN information_schema.tables AS existentes
  ON existentes.TABLE_SCHEMA = DATABASE()
  AND existentes.TABLE_NAME = requeridas.tabla
  AND existentes.TABLE_TYPE = 'BASE TABLE'
ORDER BY requeridas.tabla;

-- Si el esquema existente ya contiene todas las tablas, el siguiente paso
-- requiere configurar el baseline en Flyway y arrancar el backend. Hazlo solo
-- despues de respaldar la base y verificarla contra la migracion V1.
