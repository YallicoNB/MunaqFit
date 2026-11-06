-- ============================================================
-- Catalogo oficial de bebidas MunaqFit (etapa 3)
--
-- Fuente: Imagenes MunaqFit/bebidas.csv
-- - 21 bebidas activas: 6 que ya existian + 15 nuevas.
-- - Se conservan las categorias actuales (Energizante, Detox,
--   Relajante, Proteico, Hidratante, Antiinflamatorio, Clasico);
--   NO se introducen FRIA/CALIENTE/ESPECIAL.
-- - Las 6 bebidas antiguas que no estan en el catalogo nuevo se
--   desactivan, NO se borran: siguen existiendo para el historial
--   de ventas, recetas y reportes.
-- - Las 6 que ya existian se actualizan por nombre para conservar
--   su id (y con el, sus ventas y recetas).
-- - Las imagenes viven en frontend/src/app/assets/bebidas y se
--   publican como /assets/bebidas/... (URL relativa del CSV).
-- ============================================================

-- 1. Asegurar que existan las categorias que usa el catalogo.
--    INSERT IGNORE no crea duplicados porque categoria_bebida.nombre es UNIQUE.
INSERT IGNORE INTO categoria_bebida (nombre, descripcion) VALUES
    ('Energizante',      'Bebidas para energía y rendimiento'),
    ('Detox',            'Bebidas depurativas'),
    ('Relajante',        'Bebidas para calma y descanso'),
    ('Proteico',         'Bebidas con aporte proteico'),
    ('Hidratante',       'Bebidas para hidratación'),
    ('Antiinflamatorio', 'Bebidas con ingredientes antiinflamatorios'),
    ('Clasico',          'Frappés y bebidas clásicas');

-- 2. Actualizar las 6 bebidas que ya existian (conservan id, categoria y recetas).
UPDATE bebida SET descripcion = 'Sabor a naranja y un shot de energía.',            precio = 16.00, imagen_url = 'assets/bebidas/atomic.jpg',        activo = TRUE WHERE nombre = 'Atomic';
UPDATE bebida SET descripcion = 'Sabor a manzana; aporta fibra al cuerpo.',          precio = 16.00, imagen_url = 'assets/bebidas/detox.jpg',         activo = TRUE WHERE nombre = 'Detox';
UPDATE bebida SET descripcion = 'Sabor a menta con hierbas relajantes.',             precio = 16.00, imagen_url = 'assets/bebidas/relax.jpg',         activo = TRUE WHERE nombre = 'Relax';
UPDATE bebida SET descripcion = 'Sabor a fresa con 10 g de proteína y colágeno.',    precio = 16.00, imagen_url = 'assets/bebidas/pink-protein.jpg',  activo = TRUE WHERE nombre = 'Pink Protein';
UPDATE bebida SET descripcion = 'Sabor a frutos rojos con electrolitos.',            precio = 16.00, imagen_url = 'assets/bebidas/hydrate.jpg',      activo = TRUE WHERE nombre = 'Hydrate';
UPDATE bebida SET descripcion = 'Piña, limón y jengibre a base de cúrcuma.',         precio = 16.00, imagen_url = 'assets/bebidas/golden-glow.jpg',  activo = TRUE WHERE nombre = 'Golden Glow';

-- 3. Insertar las 6 anteriores solo si la base es nueva (no habia datos).
INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Atomic', 'Sabor a naranja y un shot de energía.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Energizante'), 'assets/bebidas/atomic.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Atomic');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Detox', 'Sabor a manzana; aporta fibra al cuerpo.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Detox'), 'assets/bebidas/detox.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Detox');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Relax', 'Sabor a menta con hierbas relajantes.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Relajante'), 'assets/bebidas/relax.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Relax');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Pink Protein', 'Sabor a fresa con 10 g de proteína y colágeno.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Proteico'), 'assets/bebidas/pink-protein.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Pink Protein');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Hydrate', 'Sabor a frutos rojos con electrolitos.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Hidratante'), 'assets/bebidas/hydrate.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Hydrate');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Golden Glow', 'Piña, limón y jengibre a base de cúrcuma.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Antiinflamatorio'), 'assets/bebidas/golden-glow.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Golden Glow');

-- 4. Frappés premium (S/ 19). Se agrupan en la categoria actual "Clasico".
UPDATE bebida SET descripcion = 'Frappé de naranja con 27 g de proteína.',             precio = 19.00, imagen_url = 'assets/bebidas/naranja-champ.jpg',       activo = TRUE WHERE nombre = 'Naranja Champ';
UPDATE bebida SET descripcion = 'Frappé de brownie con 27 g de proteína.',             precio = 19.00, imagen_url = 'assets/bebidas/brownie.jpg',              activo = TRUE WHERE nombre = 'Brownie';
UPDATE bebida SET descripcion = 'Frappé de pie de limón con 27 g de proteína.',        precio = 19.00, imagen_url = 'assets/bebidas/pie-de-limon.jpg',         activo = TRUE WHERE nombre = 'Pie de limón';
UPDATE bebida SET descripcion = 'Frappé de caramelo con 27 g de proteína.',            precio = 19.00, imagen_url = 'assets/bebidas/caramel.jpg',              activo = TRUE WHERE nombre = 'Caramel';
UPDATE bebida SET descripcion = 'Frappé de Oreo con 27 g de proteína.',                precio = 19.00, imagen_url = 'assets/bebidas/oreo.jpg',                 activo = TRUE WHERE nombre = 'Oreo';
UPDATE bebida SET descripcion = 'Frappé de pie de manzana con 27 g de proteína.',      precio = 19.00, imagen_url = 'assets/bebidas/pie-de-manzana.jpg',       activo = TRUE WHERE nombre = 'Pie de manzana';
UPDATE bebida SET descripcion = 'Frappé Mocaccino con 27 g de proteína.',              precio = 19.00, imagen_url = 'assets/bebidas/mocaccino.jpg',            activo = TRUE WHERE nombre = 'Mocaccino';
UPDATE bebida SET descripcion = 'Frappé de cheesecake de fresa con 27 g de proteína.', precio = 19.00, imagen_url = 'assets/bebidas/cheesecake-de-fresa.jpg',  activo = TRUE WHERE nombre = 'Cheesecake de Fresa';
UPDATE bebida SET descripcion = 'Frappé Sublime Fit con 27 g de proteína.',            precio = 19.00, imagen_url = 'assets/bebidas/sublime-fit.jpg',          activo = TRUE WHERE nombre = 'Sublime Fit';
UPDATE bebida SET descripcion = 'Frappé de caramelo con 27 g de proteína.',            precio = 19.00, imagen_url = 'assets/bebidas/caramel-frappuccino.jpg',  activo = TRUE WHERE nombre = 'Caramel Frappuccino';

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Naranja Champ', 'Frappé de naranja con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/naranja-champ.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Naranja Champ');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Brownie', 'Frappé de brownie con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/brownie.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Brownie');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Pie de limón', 'Frappé de pie de limón con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/pie-de-limon.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Pie de limón');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Caramel', 'Frappé de caramelo con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/caramel.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Caramel');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Oreo', 'Frappé de Oreo con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/oreo.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Oreo');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Pie de manzana', 'Frappé de pie de manzana con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/pie-de-manzana.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Pie de manzana');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Mocaccino', 'Frappé Mocaccino con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/mocaccino.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Mocaccino');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Cheesecake de Fresa', 'Frappé de cheesecake de fresa con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/cheesecake-de-fresa.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Cheesecake de Fresa');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Sublime Fit', 'Frappé Sublime Fit con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/sublime-fit.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Sublime Fit');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Caramel Frappuccino', 'Frappé de caramelo con 27 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/caramel-frappuccino.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Caramel Frappuccino');

-- 5. Bebidas calientes (S/ 19), tambien en "Clasico".
UPDATE bebida SET descripcion = 'Mocca Latte caliente con 25 g de proteína.',   precio = 19.00, imagen_url = 'assets/bebidas/mocca-latte.jpg',   activo = TRUE WHERE nombre = 'Mocca Latte';
UPDATE bebida SET descripcion = 'Caramel Latte caliente con 25 g de proteína.', precio = 19.00, imagen_url = 'assets/bebidas/caramel-latte.jpg', activo = TRUE WHERE nombre = 'Caramel Latte';
UPDATE bebida SET descripcion = 'Chocolate caliente con 25 g de proteína.',     precio = 19.00, imagen_url = 'assets/bebidas/hot-chocolate.jpg', activo = TRUE WHERE nombre = 'Hot Chocolate';

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Mocca Latte', 'Mocca Latte caliente con 25 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/mocca-latte.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Mocca Latte');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Caramel Latte', 'Caramel Latte caliente con 25 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/caramel-latte.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Caramel Latte');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'Hot Chocolate', 'Chocolate caliente con 25 g de proteína.', 19.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Clasico'), 'assets/bebidas/hot-chocolate.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'Hot Chocolate');

-- 6. Especiales (sin foto): PRE usa Energizante y POST usa Proteico.
UPDATE bebida SET descripcion = 'Con cafeína natural, vitaminas B y C, y electrolitos.', precio = 16.00, imagen_url = NULL, activo = TRUE WHERE nombre = 'PRE ENTRENO';
UPDATE bebida SET descripcion = 'Con 25 g de proteína y vitaminas para recuperarte.',    precio = 18.00, imagen_url = NULL, activo = TRUE WHERE nombre = 'POST ENTRENO';

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'PRE ENTRENO', 'Con cafeína natural, vitaminas B y C, y electrolitos.', 16.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Energizante'), NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'PRE ENTRENO');

INSERT INTO bebida (nombre, descripcion, precio, categoria_id, imagen_url, activo)
SELECT 'POST ENTRENO', 'Con 25 g de proteína y vitaminas para recuperarte.', 18.00, (SELECT id FROM categoria_bebida WHERE nombre = 'Proteico'), NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bebida WHERE nombre = 'POST ENTRENO');

-- 7. Desactivar las 6 bebidas que no forman parte del catalogo nuevo.
--    No se borran: sus ventas, recetas y reportes historicos siguen intactos.
UPDATE bebida
SET activo = FALSE
WHERE nombre IN ('Classic', 'Cheese', 'Pizza', 'Base Manjar', 'Base Queso', 'Base Chocolate');
