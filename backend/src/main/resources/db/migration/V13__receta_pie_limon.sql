-- Correccion: la bebida se llama "Pie de limón" (con tilde), por lo que
-- las filas de V12 que usaban "Pie de limon" no encontraron la bebida.
-- Esta migracion inserta/actualiza su receta con el nombre exacto.
INSERT INTO receta (bebida_id, producto_id, cantidad, paso_instruccion)
SELECT b.id, p.id, r.cantidad, 'Licuar con hielo hasta textura cremosa.'
FROM (
    SELECT 'Limon'        AS insumo, 1.000 AS cantidad
    UNION ALL SELECT 'Leche',        0.200
    UNION ALL SELECT 'Proteina whey', 0.030
    UNION ALL SELECT 'Endulzante',   0.010
    UNION ALL SELECT 'Hielo',        0.200
) r
JOIN bebida b ON b.nombre = 'Pie de limón'
JOIN producto p ON p.nombre = r.insumo
ON DUPLICATE KEY UPDATE
    cantidad = VALUES(cantidad),
    paso_instruccion = VALUES(paso_instruccion);
