-- ============================================================
-- Datos minimos para un despliegue desde cero.
--
-- Flyway (V1-V14) crea el esquema, el catalogo de 21 bebidas y los
-- insumos/recetas. Lo que antes aportaba database/seed.sql y no puede
-- ejecutarse en produccion (hace TRUNCATE del catalogo) se agrega aqui:
--   - parametro IGV (lo lee VentaService),
--   - usuarios demo para poder iniciar sesion,
--   - proveedores de ejemplo.
--
-- Todo es idempotente: si la fila ya existe, no se duplica ni se
-- sobrescribe. Se puede aplicar sobre munaqfit_dev sin perder datos.
-- ============================================================

-- 1. Parametro IGV (0.18 = 18 %).
INSERT INTO parametro (codigo, nombre, valor, tipo_dato, descripcion)
SELECT 'IGV', 'Impuesto General a las Ventas', '0.18', 'DECIMAL', 'Tasa de IGV vigente (Peru)'
WHERE NOT EXISTS (SELECT 1 FROM parametro WHERE codigo = 'IGV');

-- 2. Usuarios demo (password de todos: 12345678).
INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado)
SELECT '12345678', 'Noe Munaq', 'noe.munaq@munaqfit.com',
       '$2a$10$zctytCR0prc/fKewL.3dW.HOlIAb8Iwvebumq5QXET/YZz5.RCFN2', 'ADMIN', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE dni = '12345678');

INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado)
SELECT '87654321', 'Noe Developer', 'noe@munaqfit.com',
       '$2a$10$Ba81nWeeqlSpFneLvjnn5ODZLnp1.YDe.lsupnkda.x0z9nFmvACu', 'EMPLEADO', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE dni = '87654321');

INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado)
SELECT '11111111', 'Maria Lopez', 'maria@munaqfit.com',
       '$2a$10$cJvAEVw.SDiJa/Z0uz5CXO98CTsXzQWfDUxCaBKxrL6PlimWks1yK', 'EMPLEADO', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE dni = '11111111');

INSERT INTO usuario (dni, nombre_completo, email, password, rol, estado)
SELECT '22222222', 'Carlos Ruiz', 'carlos@munaqfit.com',
       '$2a$10$DmdWOt7RK2QS4l4xH5l4NeVCICTMTLD2RnIhu.U8lBHWqpPfLyIB2', 'EMPLEADO', 'INACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE dni = '22222222');

-- 3. Proveedores de ejemplo para que el CRUD no arranque vacio.
INSERT INTO proveedor (nombre, ruc, telefono, direccion, contacto_nombre, estado, tipo_contrato)
SELECT 'Distribuidora LaVictoria', '20123456789', '999111222', 'Av. Grau 100', 'Jorge Torres', 'ACTIVO', 'FIJO'
WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE nombre = 'Distribuidora LaVictoria');

INSERT INTO proveedor (nombre, ruc, telefono, direccion, contacto_nombre, estado, tipo_contrato)
SELECT 'Frutas del Sol SAC', '20567890123', '988333444', 'Jr. Amazonas 45', 'Lucia Rios', 'ACTIVO', 'VARIABLE'
WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE nombre = 'Frutas del Sol SAC');

INSERT INTO proveedor (nombre, ruc, telefono, direccion, contacto_nombre, estado, tipo_contrato)
SELECT 'Prote Colombia', '20678901234', '977555666', 'Calle Lima 200', 'Pedro Gomez', 'ACTIVO', 'FIJO'
WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE nombre = 'Prote Colombia');
