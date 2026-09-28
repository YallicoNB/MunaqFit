-- ============================================================
-- V3__datos_nutricionales.sql
-- Informacion nutricional por bebida (REQ-018).
--
-- Las columnas nacen NULL a proposito: Dev 4 completa los valores y
-- hace el render (cuando no hay dato se muestra "Informacion
-- nutricional no disponible"). Los CHECK solo garantizan que si se
-- carga un numero, no sea negativo.
-- ============================================================

ALTER TABLE bebida
  ADD COLUMN calorias      DECIMAL(8,2) NULL CHECK (calorias      IS NULL OR calorias      >= 0),
  ADD COLUMN proteinas     DECIMAL(8,2) NULL CHECK (proteinas     IS NULL OR proteinas     >= 0),
  ADD COLUMN carbohidratos DECIMAL(8,2) NULL CHECK (carbohidratos IS NULL OR carbohidratos >= 0),
  ADD COLUMN grasas        DECIMAL(8,2) NULL CHECK (grasas        IS NULL OR grasas        >= 0),
  ADD COLUMN fibra         DECIMAL(8,2) NULL CHECK (fibra         IS NULL OR fibra         >= 0),
  ADD COLUMN azucares      DECIMAL(8,2) NULL CHECK (azucares      IS NULL OR azucares      >= 0);