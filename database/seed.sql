-- ============================================================
-- MUNAQ FIT MANAGER - SEED (datos de prueba)
-- Motor: MySQL 8.0+
-- ============================================================

 create database munaqfid;
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
TRUNCATE TABLE producto;
TRUNCATE TABLE proveedor;
TRUNCATE TABLE categoria;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- USUARIOS (password de todos: 12345678)
-- ============================================================
INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado) VALUES
('12345678', 'Elvis Munaq', 'elvis@munaqfit.com', '$2a$10$zctytCR0prc/fKewL.3dW.HOlIAb8Iwvebumq5QXET/YZz5.RCFN2', 'ADMIN', 'ACTIVO'),
('87654321', 'Noe Developer', 'noe@munaqfit.com',   '$2a$10$Ba81nWeeqlSpFneLvjnn5ODZLnp1.YDe.lsupnkda.x0z9nFmvACu', 'EMPLEADO', 'ACTIVO'),
('11111111', 'Maria Lopez',   'maria@munaqfit.com', '$2a$10$cJvAEVw.SDiJa/Z0uz5CXO98CTsXzQWfDUxCaBKxrL6PlimWks1yK', 'EMPLEADO', 'ACTIVO'),
('22222222', 'Carlos Ruiz',   'carlos@munaqfit.com', '$2a$10$DmdWOt7RK2QS4l4xH5l4NeVCICTMTLD2RnIhu.U8lBHWqpPfLyIB2', 'EMPLEADO', 'INACTIVO');

-- ============================================================
-- CATEGORIAS
-- ============================================================
INSERT INTO categoria (nombre, descripcion) VALUES
('Frutas', 'Frutas frescas'),
('Lacteos', 'Leches y derivados'),
('Proteinas', 'Suplementos de proteina'),
('Granos', 'Avena, chia, linaza'),
('Endulzantes', 'Miel, stevia, azucar'),
('Otros', 'Insumos varios');

-- ============================================================
-- PROVEEDORES
-- ============================================================
INSERT INTO proveedor (nombre, ruc, telefono, direccion, contacto_nombre, estado, tipo_contrato) VALUES
('Distribuidora LaVictoria', '20123456789', '999111222', 'Av. Grau 100', 'Jorge Torres', 'ACTIVO', 'FIJO'),
('Frutas del Sol SAC', '20567890123', '988333444', 'Jr. Amazonas 45', 'Lucia Rios', 'ACTIVO', 'VARIABLE'),
('Prote Colombia', '20678901234', '977555666', 'Calle Lima 200', 'Pedro Gomez', 'ACTIVO', 'FIJO');

-- ============================================================
-- PRODUCTOS (Insumos)
-- ============================================================
INSERT INTO producto (nombre, categoria_id, proveedor_id, stock_actual, stock_minimo, stock_critico, unidad_medida, costo_unitario, precio_venta) VALUES
('Platano',        1, 2, 50.00, 10.00, 5.00,  'UNIDAD', 0.80, NULL),
('Fresa',          1, 2, 30.00, 8.00,  4.00,  'G',      0.05, NULL),
('Naranja',        1, 2, 40.00, 10.00, 5.00,  'UNIDAD', 0.60, NULL),
('Manzana',        1, 2, 35.00, 8.00,  4.00,  'UNIDAD', 0.70, NULL),
('Pina',           1, 2, 20.00, 5.00,  3.00,  'UNIDAD', 2.50, NULL),
('Leche',          2, 1, 15.00, 5.00,  2.00,  'L',      3.80, NULL),
('Yogurt griego',  2, 1, 12.00, 4.00,  2.00,  'KG',     12.00, NULL),
('Proteina whey',  3, 3, 20.00, 5.00,  2.00,  'KG',     90.00, NULL),
('Avena',          4, 1, 18.00, 5.00,  2.00,  'KG',     6.50, NULL),
('Chia',           4, 1, 10.00, 3.00,  1.00,  'KG',     18.00, NULL),
('Miel',           5, 1, 8.00,  2.00,  1.00,  'L',      22.00, NULL),
('Espinaca',       1, 2, 25.00, 8.00,  4.00,  'G',      0.02, NULL);

-- ============================================================
-- BEBIDAS
-- ============================================================
INSERT INTO bebida (nombre, descripcion, precio, categoria, tiempo_preparacion, activo) VALUES
('Atomic',    'Saborear a naranja, shot de energia', 16.00, 'Energizante', 3, TRUE),
('Detox',     'Verde desintoxicante con espinaca',   16.00, 'Detox', 4, TRUE),
('Relax',     'Relajante con platano, calma y sueno', 16.00, 'Relajante', 3, TRUE),
('Pink Protein', 'Proteico rosa con fresa',          16.00, 'Proteico', 4, TRUE),
('Hydrate',   'Hidratante con pina y coco',          16.00, 'Hidratante', 3, TRUE),
('Golden Glow', 'Antiinflamatorio con mango',        16.00, 'Antiinflamatorio', 4, TRUE),
('Classic',   'Batido clasico de platano',           16.00, 'Clasico', 3, TRUE),
('Cheese',    'Batido con queso',                    16.00, 'Clasico', 3, TRUE),
('Pizza',     'Batido especial pizza',               16.00, 'Clasico', 4, TRUE),
('Base Manjar',  'Base de proteina manjar',          16.00, 'Base', 2, TRUE),
('Base Queso',   'Base de proteina queso',           16.00, 'Base', 2, TRUE),
('Base Chocolate','Base de proteina chocolate',      16.00, 'Base', 2, TRUE);

-- ============================================================
-- RECETAS
-- ============================================================
INSERT INTO receta (bebida_id, producto_id, cantidad, unidad, paso_instruccion) VALUES
(1, 3, 2.00, 'UNIDAD', 'Exprimir jugo de naranja y mezclar con shot de energia'),
(1, 6, 0.30, 'L',      'Agregar leche y batir'),
(2, 12, 80.00, 'G',    'Licuar espinaca con agua'),
(2, 1, 1.00, 'UNIDAD', 'Anadir platano'),
(3, 1, 2.00, 'UNIDAD', 'Usar platano maduro para efecto relajante'),
(3, 9, 0.05, 'KG',     'Avena en la mezcla'),
(4, 8, 0.03, 'KG',     'Proteina whey sabor fresa'),
(4, 2, 80.00, 'G',     'Fresas frescas'),
(5, 5, 0.20, 'UNIDAD', 'Pina en trozos'),
(6, 11, 0.03, 'L',     'Miel para endulzar');

-- ============================================================
-- CLIENTES FIDELIDAD
-- ============================================================
INSERT INTO cliente_fidelidad (nombre, telefono, email, visitas, umbral_premio) VALUES
('Ana Torres', '987654321', 'ana@gmail.com', 8, 10),
('Luis Perez', '912345678', 'luis@gmail.com', 10, 10),
('Sofia Diaz', '954321987', NULL, 3, 10);
