-- ============================================================
-- Reproducibilidad del catalogo de insumos y recetas.
--
-- En una base nueva, V12 se ejecuta antes de que existan las
-- categorias base (Granos, Frutas, Lacteos, Endulzantes, Proteinas),
-- por lo que sus INSERT ... SELECT no encontraban filas. V14 asegura
-- esas categorias y vuelve a ejecutar los mismos INSERT idempotentes
-- (producto: WHERE NOT EXISTS; receta: ON DUPLICATE KEY UPDATE).
-- En la base de desarrollo ya existen, asi que solo actualiza.
-- ============================================================

INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
    ('Granos',      'Cereales y granos'),
    ('Frutas',      'Frutas frescas'),
    ('Lacteos',     'Leche, yogurt y derivados'),
    ('Endulzantes', 'Miel, azucar, cacao y siropes'),
    ('Proteinas',   'Proteina whey y colageno');
-- ============================================================
-- Recetas de las 21 bebidas activas (etapa 3)
--
-- Fuentes de referencia (recetas genericas, adaptadas a los insumos
-- disponibles en MunaqFit):
--   - Buena Vibra: frappe de chocolate y proteina (200 ml leche,
--     2 scoops de proteina, hielo).
--   - Nescafe: frappe de brownie (cafe con leche, 35 g brownie,
--     12 cubos de hielo).
--   - Dymatize: frappe de proteina y moca.
--
-- Las cantidades son una base operativa; el dueno puede ajustarlas.
-- Se usa INSERT ... ON DUPLICATE KEY para no duplicar una receta que
-- ya exista para la misma bebida e insumo.
-- ============================================================

-- 1. Categorias nuevas para los insumos que no encajan en las actuales.
INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
    ('Panaderia', 'Brownies, galletas y productos de reposteria'),
    ('Insumos',   'Hielo, endulzantes y otros insumos de preparacion');

-- 2. Insumos nuevos. Si ya existen por nombre, no se tocan.
INSERT INTO producto (nombre, categoria_id, stock_actual, stock_minimo, stock_critico,
                      unidad_medida, costo_unitario, ultima_reposicion)
SELECT r.nombre, c.id, r.stock, r.minimo, r.critico, r.unidad, r.costo, NOW()
FROM (
    SELECT 'Cafe molido'          AS nombre, 'Granos'      AS categoria, 5.000  AS stock, 1.000 AS minimo, 0.500 AS critico, 'KG'     AS unidad, 45.0000 AS costo
    UNION ALL SELECT 'Cacao en polvo',      'Endulzantes', 3.000, 0.500, 0.200, 'KG', 30.0000
    UNION ALL SELECT 'Sirope de caramelo',  'Endulzantes', 5.000, 1.000, 0.500, 'L',  18.0000
    UNION ALL SELECT 'Brownie',             'Panaderia',  30.000, 6.000, 3.000, 'UNIDAD', 4.5000
    UNION ALL SELECT 'Galleta Oreo',        'Panaderia',  60.000, 12.000, 6.000, 'UNIDAD', 1.0000
    UNION ALL SELECT 'Queso crema',         'Lacteos',     3.000, 0.500, 0.200, 'KG', 22.0000
    UNION ALL SELECT 'Limon',               'Frutas',     40.000, 8.000, 4.000, 'UNIDAD', 0.5000
    UNION ALL SELECT 'Canela en polvo',     'Endulzantes', 0.500, 0.100, 0.050, 'KG', 40.0000
    UNION ALL SELECT 'Crema chantilly',     'Lacteos',     3.000, 0.500, 0.200, 'L',  15.0000
    UNION ALL SELECT 'Hielo',               'Insumos',    50.000, 10.000, 5.000, 'KG', 0.5000
    UNION ALL SELECT 'Colageno',            'Proteinas',   2.000, 0.300, 0.100, 'KG', 80.0000
    UNION ALL SELECT 'Electrolitos',        'Insumos',     2.000, 0.300, 0.100, 'KG', 30.0000
    UNION ALL SELECT 'Endulzante',          'Insumos',     5.000, 1.000, 0.500, 'KG', 12.0000
) r
JOIN categoria c ON c.nombre = r.categoria
WHERE NOT EXISTS (SELECT 1 FROM producto p WHERE p.nombre = r.nombre);

-- 3. Recetas. Cada fila es (bebida, insumo, cantidad, paso).
--    Se actualiza la cantidad si la pareja bebida-insumo ya existia.
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
