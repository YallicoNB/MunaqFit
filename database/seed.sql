-- ============================================================
-- MUNAQ FIT MANAGER - SEED (datos de prueba)
-- Motor: MySQL 8.0+
--
-- HOY EL ESQUEMA LO CREA FLYWAY (backend/src/main/resources/db/migration).
-- Este archivo solo carga los datos: se ejecuta DESPUES de arrancar el
-- backend una vez (Flyway deja las tablas listas). Es re-ejecutable:
-- los TRUNCATE lo reinician siempre al mismo estado.
-- ============================================================

USE munaqfit;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE visita_cliente;
TRUNCATE TABLE cliente_fidelidad;
TRUNCATE TABLE movimiento_inventario;
TRUNCATE TABLE pago;
TRUNCATE TABLE detalle_venta;
TRUNCATE TABLE venta;
TRUNCATE TABLE receta;
TRUNCATE TABLE bebida;
TRUNCATE TABLE producto_proveedor;
TRUNCATE TABLE producto;
TRUNCATE TABLE proveedor;
TRUNCATE TABLE categoria_bebida;
TRUNCATE TABLE categoria;
TRUNCATE TABLE parametro;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- PARAMETROS
-- La tasa de IGV vive aqui; VentaService la lee de esta tabla.
-- ============================================================
INSERT INTO parametro (codigo, nombre, valor, tipo_dato, descripcion) VALUES
('IGV', 'Impuesto General a las Ventas', '0.18', 'DECIMAL', 'Tasa de IGV vigente (Peru)');

-- ============================================================
-- USUARIOS (password de todos: 12345678)
-- ============================================================
INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado) VALUES
('12345678', 'Noe Munaq', 'noe.munaq@munaqfit.com', '$2a$10$zctytCR0prc/fKewL.3dW.HOlIAb8Iwvebumq5QXET/YZz5.RCFN2', 'ADMIN', 'ACTIVO'),
('87654321', 'Noe Developer', 'noe@munaqfit.com',   '$2a$10$Ba81nWeeqlSpFneLvjnn5ODZLnp1.YDe.lsupnkda.x0z9nFmvACu', 'EMPLEADO', 'ACTIVO'),
('11111111', 'Maria Lopez',   'maria@munaqfit.com', '$2a$10$cJvAEVw.SDiJa/Z0uz5CXO98CTsXzQWfDUxCaBKxrL6PlimWks1yK', 'EMPLEADO', 'ACTIVO'),
('22222222', 'Carlos Ruiz',   'carlos@munaqfit.com', '$2a$10$DmdWOt7RK2QS4l4xH5l4NeVCICTMTLD2RnIhu.U8lBHWqpPfLyIB2', 'EMPLEADO', 'INACTIVO');

-- ============================================================
-- CATEGORIAS DE INSUMO
-- ============================================================
INSERT INTO categoria (id, nombre, descripcion) VALUES
(1, 'Frutas',      'Frutas frescas'),
(2, 'Lacteos',     'Leches y derivados'),
(3, 'Proteinas',   'Suplementos de proteina'),
(4, 'Granos',      'Avena, chia, linaza'),
(5, 'Endulzantes', 'Miel, stevia, azucar'),
(6, 'Otros',       'Insumos varios');

-- ============================================================
-- CATEGORIAS DE BEBIDA
-- ============================================================
INSERT INTO categoria_bebida (id, nombre, descripcion) VALUES
(1, 'Energizante',     'Aportan energia inmediata'),
(2, 'Detox',           'Desintoxican y depuran'),
(3, 'Relajante',       'Ayudan a descansar y a dormir mejor'),
(4, 'Proteico',        'Altas en proteina para recuperacion muscular'),
(5, 'Hidratante',      'Reponen liquidos y vitaminas'),
(6, 'Antiinflamatorio','Apoyan la recuperacion de inflamaciones'),
(7, 'Clasico',         'Recetas de siempre, la Favorita del publico'),
(8, 'Base',            'Bases de proteina para otras preparaciones');

-- ============================================================
-- PROVEEDORES
-- ============================================================
INSERT INTO proveedor (nombre, ruc, telefono, direccion, contacto_nombre, estado, tipo_contrato) VALUES
('Distribuidora LaVictoria', '20123456789', '999111222', 'Av. Grau 100', 'Jorge Torres', 'ACTIVO', 'FIJO'),
('Frutas del Sol SAC', '20567890123', '988333444', 'Jr. Amazonas 45', 'Lucia Rios', 'ACTIVO', 'VARIABLE'),
('Prote Colombia', '20678901234', '977555666', 'Calle Lima 200', 'Pedro Gomez', 'ACTIVO', 'FIJO');

-- ============================================================
-- PRODUCTOS (Insumos)
-- Sin proveedor_id: desde V2 la relacion con proveedores vive en
-- producto_proveedor (un insumo puede comprarse a varios).
-- ============================================================
INSERT INTO producto (id, nombre, categoria_id, stock_actual, stock_minimo, stock_critico, unidad_medida, costo_unitario) VALUES
(1,  'Platano',        1, 50.000,  10.00,  5.00,  'UNIDAD', 0.80),
(2,  'Fresa',          1, 500.000, 150.00, 80.00, 'G',      0.015),
(3,  'Naranja',        1, 40.000,  10.00,  5.00,  'UNIDAD', 0.60),
(4,  'Manzana',        1, 35.000,   8.00,  4.00,  'UNIDAD', 0.70),
(5,  'Pina',           1, 20.000,   5.00,  3.00,  'UNIDAD', 2.50),
(6,  'Leche',          2, 15.000,   5.00,  2.00,  'L',      3.80),
(7,  'Yogurt griego',  2, 12.000,   4.00,  2.00,  'KG',    12.00),
(8,  'Proteina whey',  3, 20.000,   5.00,  2.00,  'KG',    90.00),
(9,  'Avena',          4, 18.000,   5.00,  2.00,  'KG',     6.50),
(10, 'Chia',           4, 10.000,   3.00,  1.00,  'KG',    18.00),
(11, 'Miel',           5, 8.000,    2.00,  1.00,  'L',     22.00),
(12, 'Espinaca',       1, 400.000, 100.00, 50.00, 'G',      0.008);

-- ============================================================
-- PRODUCTO_PROVEEDOR (puente N-N)
-- Cada insumo conserva su proveedor de la etapa 1 como principal,
-- con el mismo precio al que se compraba (precio_unitario).
-- ============================================================
INSERT INTO producto_proveedor (producto_id, proveedor_id, precio_unitario, es_principal) VALUES
(1,  2, 0.8000, 1),
(2,  2, 0.0150, 1),
(3,  2, 0.6000, 1),
(4,  2, 0.7000, 1),
(5,  2, 2.5000, 1),
(6,  1, 3.8000, 1),
(7,  1, 12.0000, 1),
(8,  3, 90.0000, 1),
(9,  1, 6.5000, 1),
(10, 1, 18.0000, 1),
(11, 1, 22.0000, 1),
(12, 2, 0.0080, 1);

-- ============================================================
-- BEBIDAS
-- ============================================================
INSERT INTO bebida (id, nombre, descripcion, precio, categoria_id, tiempo_preparacion, activo) VALUES
(1,  'Atomic',         'Saborear a naranja, shot de energia',   16.00, 1, 3, TRUE),
(2,  'Detox',          'Verde desintoxicante con espinaca',     16.00, 2, 4, TRUE),
(3,  'Relax',          'Relajante con platano, calma y sueno',  16.00, 3, 3, TRUE),
(4,  'Pink Protein',   'Proteico rosa con fresa',                16.00, 4, 4, TRUE),
(5,  'Hydrate',        'Hidratante con pina y coco',             16.00, 5, 3, TRUE),
(6,  'Golden Glow',    'Antiinflamatorio con mango',             16.00, 6, 4, TRUE),
(7,  'Classic',        'Batido clasico de platano',              16.00, 7, 3, TRUE),
(8,  'Cheese',         'Batido con queso',                       16.00, 7, 3, TRUE),
(9,  'Pizza',          'Batido especial pizza',                  16.00, 7, 4, TRUE),
(10, 'Base Manjar',    'Base de proteina manjar',                16.00, 8, 2, TRUE),
(11, 'Base Queso',     'Base de proteina queso',                 16.00, 8, 2, TRUE),
(12, 'Base Chocolate', 'Base de proteina chocolate',              16.00, 8, 2, TRUE);

-- ============================================================
-- RECETAS
-- ============================================================
INSERT INTO receta (bebida_id, producto_id, cantidad, paso_instruccion) VALUES
(1, 3,  2.00,  'Exprimir jugo de naranja y mezclar con shot de energia'),
(1, 6,  0.30,  'Agregar leche y batir'),
(2, 12, 30.00, 'Licuar espinaca con agua'),
(2, 1,  1.00,  'Anadir platano'),
(3, 1,  2.00,  'Usar platano maduro para efecto relajante'),
(3, 9,  0.05,  'Avena en la mezcla'),
(4, 8,  0.03,  'Proteina whey sabor fresa'),
(4, 2,  50.00, 'Fresas frescas'),
(5, 5,  0.20,  'Pina en trozos'),
(6, 11, 0.03,  'Miel para endulzar');

-- ============================================================
-- CLIENTES FIDELIDAD
-- ============================================================
INSERT INTO cliente_fidelidad (dni, nombre, telefono, email, visitas, umbral_premio) VALUES
('44555111', 'Ana Torres', '987654321', 'ana@gmail.com',   8,  10),
('44555222', 'Luis Perez', '912345678', 'luis@gmail.com',  10, 10),
('44555333', 'Sofia Diaz', '954321987', NULL,              3,  10);