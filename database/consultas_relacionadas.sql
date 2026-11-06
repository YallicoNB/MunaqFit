-- Consultas de lectura para revisar la informacion de MunaqFit.
-- Ejecutar despues de crear la base de datos munaqfit.
-- Las relaciones se usan en los JOIN, pero no se muestran las columnas ID.
-- No se incluyen contrasenas, DNI ni otros datos que no son necesarios para
-- estos reportes. Cada consulta presenta un grupo de informacion para evitar
-- duplicados y combinaciones confusas de un unico JOIN gigante.

USE munaqfit;

-- Configuracion del negocio (tabla sin relacion con otras tablas).
SELECT
  codigo AS parametro,
  nombre,
  valor,
  tipo_dato AS tipo,
  descripcion,
  vigencia_desde AS vigente_desde
FROM parametro
ORDER BY nombre;

-- Personal y rol dentro del sistema.
SELECT
  nombre_completo AS usuario,
  rol,
  estado,
  ultimo_login,
  fecha_creacion AS creado_el
FROM usuario
ORDER BY nombre_completo;

-- Categorias de insumos.
SELECT
  nombre AS categoria,
  descripcion
FROM categoria
ORDER BY nombre;

-- Categorias de bebidas.
SELECT
  nombre AS categoria,
  descripcion
FROM categoria_bebida
ORDER BY nombre;

-- Proveedores y sus datos de contacto.
SELECT
  nombre AS proveedor,
  telefono,
  direccion,
  contacto_nombre AS contacto,
  estado,
  tipo_contrato
FROM proveedor
ORDER BY nombre;

-- Inventario de insumos relacionado con su categoria.
SELECT
  p.nombre AS insumo,
  COALESCE(c.nombre, 'Sin categoria') AS categoria,
  p.stock_actual,
  p.stock_minimo,
  p.stock_critico,
  p.unidad_medida AS unidad,
  p.costo_unitario AS costo_unitario,
  p.fecha_caducidad AS fecha_caducidad,
  p.ultima_reposicion
FROM producto p
LEFT JOIN categoria c ON c.id = p.categoria_id
ORDER BY p.nombre;

-- Relacion entre insumos y proveedores, incluyendo precio y proveedor principal.
SELECT
  p.nombre AS insumo,
  pr.nombre AS proveedor,
  pp.precio_unitario,
  CASE WHEN pp.es_principal = 1 THEN 'Si' ELSE 'No' END AS proveedor_principal
FROM producto_proveedor pp
INNER JOIN producto p ON p.id = pp.producto_id
INNER JOIN proveedor pr ON pr.id = pp.proveedor_id
ORDER BY p.nombre, pp.es_principal DESC, pr.nombre;

-- Catalogo de bebidas con su categoria e informacion nutricional.
SELECT
  b.nombre AS bebida,
  COALESCE(cb.nombre, 'Sin categoria') AS categoria,
  b.descripcion,
  b.precio,
  b.tiempo_preparacion AS minutos_preparacion,
  b.calorias,
  b.proteinas,
  b.carbohidratos,
  b.grasas,
  b.fibra,
  b.azucares,
  b.activo
FROM bebida b
LEFT JOIN categoria_bebida cb ON cb.id = b.categoria_id
ORDER BY b.nombre;

-- Recetas: ingredientes de cada bebida y cantidades requeridas.
SELECT
  b.nombre AS bebida,
  p.nombre AS insumo,
  r.cantidad,
  p.unidad_medida AS unidad,
  r.paso_instruccion AS instruccion
FROM receta r
INNER JOIN bebida b ON b.id = r.bebida_id
INNER JOIN producto p ON p.id = r.producto_id
ORDER BY b.nombre, p.nombre;

-- Ventas con el nombre del empleado que las registro.
SELECT
  v.numero_pedido AS pedido,
  v.fecha_hora,
  u.nombre_completo AS empleado,
  v.mesa,
  v.tipo_venta,
  v.subtotal,
  v.igv_tasa,
  v.igv,
  v.total,
  v.estado,
  v.notas
FROM venta v
INNER JOIN usuario u ON u.id = v.usuario_id
ORDER BY v.fecha_hora DESC;

-- Productos vendidos por pedido.
SELECT
  v.numero_pedido AS pedido,
  v.fecha_hora,
  b.nombre AS bebida,
  dv.cantidad,
  dv.precio_unitario,
  dv.subtotal
FROM detalle_venta dv
INNER JOIN venta v ON v.id = dv.venta_id
INNER JOIN bebida b ON b.id = dv.bebida_id
ORDER BY v.fecha_hora DESC, v.numero_pedido, b.nombre;

-- Pagos asociados a cada pedido.
SELECT
  v.numero_pedido AS pedido,
  v.fecha_hora AS fecha_venta,
  v.total AS total_venta,
  p.tipo_pago,
  p.monto_pagado,
  p.monto_cambio,
  p.numero_operacion,
  p.banco,
  p.fecha_pago,
  p.estado AS estado_pago
FROM pago p
INNER JOIN venta v ON v.id = p.venta_id
ORDER BY p.fecha_pago DESC;

-- Kardex: movimientos de insumos y usuario que los registro.
SELECT
  mi.fecha_movimiento,
  pr.nombre AS insumo,
  mi.tipo_movimiento,
  mi.cantidad,
  pr.unidad_medida AS unidad,
  mi.stock_anterior,
  mi.stock_nuevo,
  mi.motivo,
  mi.tipo_referencia,
  u.nombre_completo AS usuario
FROM movimiento_inventario mi
INNER JOIN producto pr ON pr.id = mi.producto_id
LEFT JOIN usuario u ON u.id = mi.usuario_id
ORDER BY mi.fecha_movimiento DESC;

-- Clientes del programa de fidelidad.
SELECT
  nombre AS cliente,
  telefono,
  email,
  visitas,
  umbral_premio,
  ultima_visita,
  fecha_registro
FROM cliente_fidelidad
ORDER BY nombre;

-- Visitas de fidelidad relacionadas con el cliente y, cuando existe, su pedido.
SELECT
  cf.nombre AS cliente,
  vc.fecha_visita,
  v.numero_pedido AS pedido,
  v.fecha_hora AS fecha_venta,
  v.total AS total_venta
FROM visita_cliente vc
INNER JOIN cliente_fidelidad cf ON cf.id = vc.cliente_fidelidad_id
LEFT JOIN venta v ON v.id = vc.venta_id
ORDER BY vc.fecha_visita DESC, cf.nombre;
