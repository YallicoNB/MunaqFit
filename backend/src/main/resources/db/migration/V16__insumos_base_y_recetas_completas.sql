-- ============================================================
-- Insumos base y recetas completas para una base desde cero.
--
-- V12 y V14 insertan los 13 insumos nuevos, pero los 12 insumos
-- historicos (leche, proteina, frutas, avena, etc.) solo existian en
-- database/seed.sql. Sin ellos, las recetas que los usan se pierden:
-- una base nueva quedaba con 13 insumos y ~49 recetas en vez de 25 y 98.
--
-- Esta migracion:
--   1. inserta los 12 insumos base si faltan (idempotente),
--   2. vuelve a ejecutar el bloque de recetas de V14, ahora que todos
--      los insumos existen (ON DUPLICATE KEY UPDATE, idempotente).
-- ============================================================

-- 1. Insumos base (los mismos de database/seed.sql).
INSERT INTO producto (nombre, categoria_id, stock_actual, stock_minimo, stock_critico,
                      unidad_medida, costo_unitario, ultima_reposicion)
SELECT r.nombre, c.id, r.stock, r.minimo, r.critico, r.unidad, r.costo, NOW()
FROM (
    SELECT 'Platano'       AS nombre, 'Frutas'      AS categoria, 50.000  AS stock, 10.000 AS minimo, 5.000 AS critico, 'UNIDAD' AS unidad, 0.8000 AS costo
    UNION ALL SELECT 'Fresa',          'Frutas',      500.000, 150.000, 80.000, 'G',      0.0150
    UNION ALL SELECT 'Naranja',        'Frutas',       40.000,  10.000,  5.000, 'UNIDAD', 0.6000
    UNION ALL SELECT 'Manzana',        'Frutas',       35.000,   8.000,  4.000, 'UNIDAD', 0.7000
    UNION ALL SELECT 'Pina',           'Frutas',       20.000,   5.000,  3.000, 'UNIDAD', 2.5000
    UNION ALL SELECT 'Leche',          'Lacteos',      15.000,   5.000,  2.000, 'L',      3.8000
    UNION ALL SELECT 'Yogurt griego',  'Lacteos',      12.000,   4.000,  2.000, 'KG',    12.0000
    UNION ALL SELECT 'Proteina whey',  'Proteinas',    20.000,   5.000,  2.000, 'KG',    90.0000
    UNION ALL SELECT 'Avena',          'Granos',       18.000,   5.000,  2.000, 'KG',     6.5000
    UNION ALL SELECT 'Chia',           'Granos',       10.000,   3.000,  1.000, 'KG',    18.0000
    UNION ALL SELECT 'Miel',           'Endulzantes',   8.000,   2.000,  1.000, 'L',     22.0000
    UNION ALL SELECT 'Espinaca',       'Frutas',      400.000, 100.000, 50.000, 'G',      0.0080
) r
JOIN categoria c ON c.nombre = r.categoria
WHERE NOT EXISTS (SELECT 1 FROM producto p WHERE p.nombre = r.nombre);

-- 2. Recetas de las 21 bebidas (mismo bloque que V14).
INSERT INTO receta (bebida_id, producto_id, cantidad, paso_instruccion)
SELECT b.id, p.id, r.cantidad, r.paso
FROM (
    -- Atomic
    SELECT 'Atomic' AS bebida, 'Leche' AS insumo, 0.300 AS cantidad, 'Licuar con hielo hasta textura cremosa.' AS paso
    UNION ALL SELECT 'Atomic', 'Naranja', 2.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Atomic', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Atomic', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Detox
    UNION ALL SELECT 'Detox', 'Espinaca', 30.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Detox', 'Platano', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Detox', 'Manzana', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Detox', 'Chia', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Detox', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Relax
    UNION ALL SELECT 'Relax', 'Avena', 0.050, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Relax', 'Platano', 2.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Relax', 'Leche', 0.250, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Relax', 'Canela en polvo', 0.005, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Relax', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Pink Protein
    UNION ALL SELECT 'Pink Protein', 'Fresa', 50.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pink Protein', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pink Protein', 'Yogurt griego', 0.100, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pink Protein', 'Colageno', 0.005, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pink Protein', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Hydrate
    UNION ALL SELECT 'Hydrate', 'Pina', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Hydrate', 'Naranja', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Hydrate', 'Electrolitos', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Hydrate', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Golden Glow
    UNION ALL SELECT 'Golden Glow', 'Miel', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Golden Glow', 'Pina', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Golden Glow', 'Limon', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Golden Glow', 'Canela en polvo', 0.005, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Golden Glow', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Naranja Champ
    UNION ALL SELECT 'Naranja Champ', 'Naranja', 2.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Naranja Champ', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Naranja Champ', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Naranja Champ', 'Miel', 0.020, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Naranja Champ', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Brownie
    UNION ALL SELECT 'Brownie', 'Brownie', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Brownie', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Brownie', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Brownie', 'Cacao en polvo', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Brownie', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Pie de limon
    UNION ALL SELECT 'Pie de limón', 'Limon', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de limón', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de limón', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de limón', 'Endulzante', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de limón', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Caramel
    UNION ALL SELECT 'Caramel', 'Sirope de caramelo', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Oreo
    UNION ALL SELECT 'Oreo', 'Galleta Oreo', 3.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Oreo', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Oreo', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Oreo', 'Cacao en polvo', 0.005, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Oreo', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Pie de manzana
    UNION ALL SELECT 'Pie de manzana', 'Manzana', 1.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de manzana', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de manzana', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de manzana', 'Canela en polvo', 0.005, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Pie de manzana', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Mocaccino
    UNION ALL SELECT 'Mocaccino', 'Cafe molido', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Mocaccino', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Mocaccino', 'Cacao en polvo', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Mocaccino', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Mocaccino', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Cheesecake de Fresa
    UNION ALL SELECT 'Cheesecake de Fresa', 'Fresa', 50.000, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Cheesecake de Fresa', 'Queso crema', 0.050, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Cheesecake de Fresa', 'Leche', 0.150, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Cheesecake de Fresa', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Cheesecake de Fresa', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Sublime Fit
    UNION ALL SELECT 'Sublime Fit', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Sublime Fit', 'Proteina whey', 0.035, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Sublime Fit', 'Avena', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Sublime Fit', 'Miel', 0.020, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Sublime Fit', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Caramel Frappuccino
    UNION ALL SELECT 'Caramel Frappuccino', 'Cafe molido', 0.010, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel Frappuccino', 'Leche', 0.200, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel Frappuccino', 'Sirope de caramelo', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel Frappuccino', 'Proteina whey', 0.030, 'Licuar con hielo hasta textura cremosa.'
    UNION ALL SELECT 'Caramel Frappuccino', 'Hielo', 0.200, 'Licuar con hielo hasta textura cremosa.'
    -- Mocca Latte
    UNION ALL SELECT 'Mocca Latte', 'Cafe molido', 0.010, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Mocca Latte', 'Leche', 0.250, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Mocca Latte', 'Cacao en polvo', 0.010, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Mocca Latte', 'Endulzante', 0.010, 'Calentar la leche y mezclar hasta integrar.'
    -- Caramel Latte
    UNION ALL SELECT 'Caramel Latte', 'Cafe molido', 0.010, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Caramel Latte', 'Leche', 0.250, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Caramel Latte', 'Sirope de caramelo', 0.030, 'Calentar la leche y mezclar hasta integrar.'
    -- Hot Chocolate
    UNION ALL SELECT 'Hot Chocolate', 'Cacao en polvo', 0.020, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Hot Chocolate', 'Leche', 0.250, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Hot Chocolate', 'Endulzante', 0.010, 'Calentar la leche y mezclar hasta integrar.'
    UNION ALL SELECT 'Hot Chocolate', 'Canela en polvo', 0.003, 'Calentar la leche y mezclar hasta integrar.'
    -- PRE ENTRENO
    UNION ALL SELECT 'PRE ENTRENO', 'Cafe molido', 0.010, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'PRE ENTRENO', 'Electrolitos', 0.010, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'PRE ENTRENO', 'Endulzante', 0.010, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'PRE ENTRENO', 'Limon', 1.000, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'PRE ENTRENO', 'Hielo', 0.200, 'Mezclar los ingredientes y servir.'
    -- POST ENTRENO
    UNION ALL SELECT 'POST ENTRENO', 'Proteina whey', 0.030, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'POST ENTRENO', 'Leche', 0.250, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'POST ENTRENO', 'Platano', 1.000, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'POST ENTRENO', 'Colageno', 0.005, 'Mezclar los ingredientes y servir.'
    UNION ALL SELECT 'POST ENTRENO', 'Hielo', 0.200, 'Mezclar los ingredientes y servir.'
) r
JOIN bebida b ON b.nombre = r.bebida
JOIN producto p ON p.nombre = r.insumo
ON DUPLICATE KEY UPDATE
    cantidad = VALUES(cantidad),
    paso_instruccion = VALUES(paso_instruccion);
