-- ============================================================
-- V4__version_optimista.sql
-- Columnas @Version (bloqueo optimista de JPA) en las dos entidades
-- donde dos empleados pueden escribir al mismo tiempo:
--   * producto -> al descontar stock por una venta simultanea;
--   * venta    -> al cobrar (pago) o cambiar el estado.
-- Hibernate compara este valor en cada UPDATE; si otra transaccion
-- ya lo cambio, la segunda recibe 409 (ver GlobalExceptionHandler).
-- ============================================================

ALTER TABLE producto ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE venta    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;