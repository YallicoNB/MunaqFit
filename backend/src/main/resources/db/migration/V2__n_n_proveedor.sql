-- ============================================================
-- V2__n_n_proveedor.sql
-- Relacion muchos-a-muchos producto <-> proveedor.
--
-- Orden obligatorio (para no perder datos):
--   1. se crea la tabla puente,
--   2. se copian los proveedores actuales de producto.proveedor_id,
--   3. recien despues se elimina la columna.
-- El precio del proveedor arranca como el costo_unitario que el insumo
-- tiene hoy (la columna precio_venta fue eliminada en la auditoria).
-- ============================================================

CREATE TABLE producto_proveedor (
  producto_id     BIGINT        NOT NULL,
  proveedor_id    BIGINT        NOT NULL,
  precio_unitario DECIMAL(10,4) NOT NULL,
  es_principal    TINYINT(1)    NOT NULL DEFAULT 1,
  PRIMARY KEY (producto_id, proveedor_id),
  CONSTRAINT fk_pp_producto  FOREIGN KEY (producto_id)  REFERENCES producto(id),
  CONSTRAINT fk_pp_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
  CONSTRAINT chk_pp_precio   CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

-- Migra los datos actuales: cada producto conserva su proveedor actual
-- como principal, con el costo que ya tenia.
INSERT INTO producto_proveedor (producto_id, proveedor_id, precio_unitario, es_principal)
SELECT id, proveedor_id, costo_unitario, 1
FROM producto
WHERE proveedor_id IS NOT NULL;

-- Indices de consulta del puente (el de producto_id lo cubre la PK).
CREATE INDEX idx_pp_proveedor ON producto_proveedor(proveedor_id);

-- La columna que quedo sin uso; los datos ya viven en la tabla puente.
-- MySQL exige soltar la FK de V1 (fk_producto_proveedor) antes de poder
-- eliminar la columna, asi que van en dos ALTERs.
ALTER TABLE producto DROP FOREIGN KEY fk_producto_proveedor;
ALTER TABLE producto DROP COLUMN proveedor_id;