-- ============================================================
-- MUNAQ FIT MANAGER - SCHEMA
-- Motor: MySQL 8.0.16+ (requiere CHECK constraints)
-- ============================================================
-- Convenciones:
--   * Toda fecha/hora es DATETIME (rango 1000-9999, sin conversion por
--     timezone). TIMESTAMP esta limitado a 2038 y se corre si cambia la
--     zona horaria del servidor.
--   * Las reglas de negocio que se pueden expresar sobre una sola tabla
--     se aplican con CHECK, no solo en Java.
--   * Las referencias entre dominios distintos usan claves foraneas.
-- ============================================================

-- ⚠️  ATENCION: esta línea BORRA la base de datos completa.
-- Es intencional para que el schema sea la única fuente de verdad y se
-- pueda reconstruir desde cero. NO la ejecutes contra una base con datos
-- reales sin respaldarla antes.
DROP DATABASE IF EXISTS munaqfit;

CREATE DATABASE IF NOT EXISTS munaqfit
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE munaqfit;

-- ============================================================
-- PARAMETRO
-- Configuracion del negocio que antes vivia hardcodeada en el codigo.
-- Permite cambiar la tasa de IGV sin recompilar.
-- ============================================================
CREATE TABLE IF NOT EXISTS parametro (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo          VARCHAR(50)   NOT NULL UNIQUE,
  nombre          VARCHAR(100)  NOT NULL,
  valor           VARCHAR(100)  NOT NULL,
  tipo_dato       ENUM('DECIMAL','ENTERO','TEXTO','BOOLEANO') NOT NULL DEFAULT 'TEXTO',
  descripcion     VARCHAR(255)  NULL,
  vigencia_desde  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_parametro_codigo CHECK (codigo <> '')
) ENGINE=InnoDB;

-- ============================================================
-- USUARIO
-- ============================================================
CREATE TABLE IF NOT EXISTS usuario (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  dni              VARCHAR(20)  NOT NULL UNIQUE,
  nombre_completo  VARCHAR(100) NOT NULL,
  email            VARCHAR(100) NOT NULL UNIQUE,
  password         VARCHAR(255) NOT NULL,
  rol              ENUM('ADMIN','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
  estado           ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  ultimo_login     DATETIME     NULL,
  fecha_creacion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  intentos_fallidos INT         NOT NULL DEFAULT 0,
  bloqueado_hasta  DATETIME     NULL,
  CONSTRAINT chk_usuario_intentos CHECK (intentos_fallidos >= 0),
  CONSTRAINT chk_usuario_dni CHECK (dni <> ''),
  CONSTRAINT chk_usuario_email CHECK (email <> '')
) ENGINE=InnoDB;

-- ============================================================
-- CATEGORIA  (clasificacion de INSUMOS)
-- Las bebidas tienen su propia taxonomia en categoria_bebida: son
-- dominios distintos y no deben mezclarse en la misma tabla.
-- ============================================================
CREATE TABLE IF NOT EXISTS categoria (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT        NULL,
  CONSTRAINT chk_categoria_nombre CHECK (nombre <> '')
) ENGINE=InnoDB;

-- ============================================================
-- CATEGORIA_BEBIDA  (clasificacion de BEBIDAS por beneficio)
-- ============================================================
CREATE TABLE IF NOT EXISTS categoria_bebida (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT        NULL,
  CONSTRAINT chk_categoria_bebida_nombre CHECK (nombre <> '')
) ENGINE=InnoDB;

-- ============================================================
-- PROVEEDOR
-- ============================================================
CREATE TABLE IF NOT EXISTS proveedor (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre          VARCHAR(100) NOT NULL,
  ruc             VARCHAR(20)  NULL,
  telefono        VARCHAR(20)  NULL,
  direccion       VARCHAR(200) NULL,
  contacto_nombre VARCHAR(100) NULL,
  estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  tipo_contrato   ENUM('FIJO','VARIABLE') NULL,
  CONSTRAINT chk_proveedor_nombre CHECK (nombre <> '')
) ENGINE=InnoDB;

-- ============================================================
-- PRODUCTO (Insumo)
-- stock_actual es un cache: la fuente de verdad es el kardex
-- (movimiento_inventario). Se mantiene en la misma transaccion.
-- ============================================================
CREATE TABLE IF NOT EXISTS producto (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre            VARCHAR(100) NOT NULL,
  categoria_id      BIGINT NULL,
  proveedor_id      BIGINT NULL,
  stock_actual      DECIMAL(12,3) NOT NULL DEFAULT 0,
  stock_minimo      DECIMAL(12,3) NOT NULL DEFAULT 0,
  stock_critico     DECIMAL(12,3) NOT NULL DEFAULT 0,
  unidad_medida     ENUM('KG','G','L','ML','UNIDAD') NOT NULL DEFAULT 'UNIDAD',
  costo_unitario    DECIMAL(10,4) NOT NULL DEFAULT 0,
  fecha_caducidad   DATE        NULL,
  ultima_reposicion DATETIME    NULL,
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
  CONSTRAINT chk_producto_nombre     CHECK (nombre <> ''),
  CONSTRAINT chk_producto_stock_actual  CHECK (stock_actual  >= 0),
  CONSTRAINT chk_producto_stock_minimo  CHECK (stock_minimo  >= 0),
  CONSTRAINT chk_producto_stock_critico CHECK (stock_critico >= 0),
  CONSTRAINT chk_producto_umbrales      CHECK (stock_critico <= stock_minimo),
  CONSTRAINT chk_producto_costo         CHECK (costo_unitario >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- BEBIDA
-- categoria_id es clave foranea: la categoria de una bebida ya no es
-- texto libre, por lo que no puede quedar huerfana ni duplicada.
-- ============================================================
CREATE TABLE IF NOT EXISTS bebida (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre             VARCHAR(100) NOT NULL,
  descripcion        TEXT         NULL,
  precio             DECIMAL(10,2) NOT NULL DEFAULT 16.00,
  categoria_id       BIGINT        NULL,
  imagen_url         VARCHAR(255)  NULL,
  tiempo_preparacion INT           NULL,
  activo             BOOLEAN       NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_bebida_categoria_bebida FOREIGN KEY (categoria_id) REFERENCES categoria_bebida(id),
  CONSTRAINT chk_bebida_nombre CHECK (nombre <> ''),
  CONSTRAINT chk_bebida_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- RECETA
-- La cantidad va siempre en la unidad canonica del producto
-- (producto.unidad_medida). Por eso no existe columna 'unidad':
-- duplicarla era lo que obligaba a convertir G<->KG y ML<->L.
-- UNIQUE impide que un insumo se repita dentro de la misma bebida,
-- que habria descontado el stock dos veces.
-- ============================================================
CREATE TABLE IF NOT EXISTS receta (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  bebida_id        BIGINT         NOT NULL,
  producto_id      BIGINT         NOT NULL,
  cantidad         DECIMAL(12,3)  NOT NULL,
  paso_instruccion TEXT           NULL,
  CONSTRAINT fk_receta_bebida   FOREIGN KEY (bebida_id)   REFERENCES bebida(id),
  CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
  CONSTRAINT uq_receta_bebida_producto UNIQUE (bebida_id, producto_id),
  CONSTRAINT chk_receta_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- ============================================================
-- VENTA
-- subtotal/igv/total e igv_tasa son instantaneas: se guardan para que
-- un reporte historico siga siendo interpretable aunque la tasa de IGV
-- cambie. igv_tasa es la que estaba hardcodeada en VentaService.
-- numero_pedido es obligatorio y unico: un ticket sin numero no es
-- un ticket.
-- ============================================================
CREATE TABLE IF NOT EXISTS venta (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id    BIGINT         NOT NULL,
  fecha_hora    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  mesa          INT            NULL,
  tipo_venta    ENUM('LOCAL','DELIVERY') NOT NULL DEFAULT 'LOCAL',
  subtotal      DECIMAL(12,2)  NOT NULL DEFAULT 0,
  igv_tasa      DECIMAL(5,4)   NOT NULL DEFAULT 0.1800,
  igv           DECIMAL(12,2)  NOT NULL DEFAULT 0,
  total         DECIMAL(12,2)  NOT NULL DEFAULT 0,
  estado        ENUM('PENDIENTE','PAGADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  notas         VARCHAR(255)   NULL,
  numero_pedido VARCHAR(30)    NOT NULL UNIQUE,
  CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT chk_venta_subtotal CHECK (subtotal >= 0),
  CONSTRAINT chk_venta_igv      CHECK (igv >= 0),
  CONSTRAINT chk_venta_total    CHECK (total >= 0),
  CONSTRAINT chk_venta_suma     CHECK (total = subtotal + igv),
  CONSTRAINT chk_venta_igv_calc CHECK (igv = ROUND(subtotal * igv_tasa, 2)),
  CONSTRAINT chk_venta_numero   CHECK (numero_pedido <> '')
) ENGINE=InnoDB;

-- ============================================================
-- DETALLE_VENTA
-- precio_unitario y subtotal son instantaneas del precio al momento
-- de la venta: cambiar el precio de una bebida no debe alterar las
-- ventas ya emitidas.
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_venta (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id        BIGINT        NOT NULL,
  bebida_id       BIGINT        NOT NULL,
  cantidad        INT           NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal        DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_detalle_venta  FOREIGN KEY (venta_id)  REFERENCES venta(id),
  CONSTRAINT fk_detalle_bebida FOREIGN KEY (bebida_id) REFERENCES bebida(id),
  CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_detalle_precio   CHECK (precio_unitario >= 0),
  CONSTRAINT chk_detalle_subtotal CHECK (subtotal = ROUND(cantidad * precio_unitario, 2))
) ENGINE=InnoDB;

-- ============================================================
-- PAGO
-- NO se guarda monto_total: se deriva de venta.total (pago -> venta ->
-- total era una dependencia transitiva y una 3FN). Tampoco se guarda
-- el vuelto, que es monto_pagado - venta.total. MySQL no permite
-- CHECK con subconsultas, asi que esa consistencia la valida PagoService.
-- ============================================================
CREATE TABLE IF NOT EXISTS pago (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id         BIGINT        NOT NULL,
  monto_pagado     DECIMAL(12,2) NOT NULL,
  monto_cambio     DECIMAL(12,2) NOT NULL DEFAULT 0,
  tipo_pago        ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','QR') NOT NULL,
  numero_operacion VARCHAR(50)   NULL,
  banco            VARCHAR(50)   NULL,
  fecha_pago       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado           ENUM('PENDIENTE','COMPLETADO') NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id) REFERENCES venta(id),
  CONSTRAINT uq_pago_venta UNIQUE (venta_id),
  CONSTRAINT chk_pago_pagado CHECK (monto_pagado > 0),
  CONSTRAINT chk_pago_cambio CHECK (monto_cambio >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- MOVIMIENTO_INVENTARIO (Kardex)
-- Fuente de verdad del stock. referencia_id es polimorfica (por eso no
-- puede tener FK), pero tipo_referencia la hace interpretable y el
-- CHECK obliga a que la pareja vaya junta o no vaya.
-- ============================================================
CREATE TABLE IF NOT EXISTS movimiento_inventario (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id      BIGINT        NOT NULL,
  tipo_movimiento  ENUM('INGRESO','SALIDA') NOT NULL,
  cantidad         DECIMAL(12,3) NOT NULL,
  stock_anterior   DECIMAL(12,3) NOT NULL,
  stock_nuevo      DECIMAL(12,3) NOT NULL,
  motivo           VARCHAR(100)  NULL,
  tipo_referencia  ENUM('VENTA','REABASTECIMIENTO','AJUSTE_MANUAL','OTRO') NULL,
  referencia_id    BIGINT        NULL,
  fecha_movimiento DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id       BIGINT        NULL,
  CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
  CONSTRAINT fk_mov_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario(id),
  CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_mov_stock    CHECK (stock_anterior >= 0 AND stock_nuevo >= 0),
  CONSTRAINT chk_mov_referencia CHECK (
    (tipo_referencia IS NULL AND referencia_id IS NULL) OR
    (tipo_referencia IS NOT NULL AND referencia_id IS NOT NULL)
  )
) ENGINE=InnoDB;

-- ============================================================
-- CLIENTE_FIDELIDAD
-- dni permite deduplicar: sin el, el mismo cliente podia registrarse
-- tantas veces como el empleado quisiera.
-- ============================================================
CREATE TABLE IF NOT EXISTS cliente_fidelidad (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  dni            VARCHAR(20)  NULL UNIQUE,
  nombre         VARCHAR(100) NOT NULL,
  telefono       VARCHAR(20)  NULL,
  email          VARCHAR(100) NULL,
  visitas        INT          NOT NULL DEFAULT 0,
  umbral_premio  INT          NOT NULL DEFAULT 10,
  ultima_visita  DATETIME     NULL,
  fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_cliente_nombre   CHECK (nombre <> ''),
  CONSTRAINT chk_cliente_visitas  CHECK (visitas >= 0),
  CONSTRAINT chk_cliente_umbral   CHECK (umbral_premio > 0)
) ENGINE=InnoDB;

-- ============================================================
-- VISITA_CLIENTE
-- UNIQUE en venta_id impide que una misma venta cuente para fidelidad
-- mas de una vez. MySQL admite varios NULL en un indice unico, asi que
-- las visitas sin venta asociada siguen siendo posibles.
-- ============================================================
CREATE TABLE IF NOT EXISTS visita_cliente (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_fidelidad_id BIGINT NOT NULL,
  venta_id             BIGINT NULL,
  fecha_visita         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_visita_cliente FOREIGN KEY (cliente_fidelidad_id) REFERENCES cliente_fidelidad(id),
  CONSTRAINT fk_visita_venta   FOREIGN KEY (venta_id) REFERENCES venta(id),
  CONSTRAINT uq_visita_venta   UNIQUE (venta_id)
) ENGINE=InnoDB;

-- ============================================================
-- INDICES
-- Los indices de las claves foraneas los crea InnoDB solo; aqui van
-- los que corresponden a las consultas de los reportes.
-- ============================================================
CREATE INDEX idx_producto_nombre        ON producto(nombre);
CREATE INDEX idx_producto_categoria      ON producto(categoria_id, nombre);
CREATE INDEX idx_bebida_nombre          ON bebida(nombre);
CREATE INDEX idx_bebida_categoria       ON bebida(categoria_id);
CREATE INDEX idx_receta_producto        ON receta(producto_id);
CREATE INDEX idx_venta_fecha            ON venta(fecha_hora);
CREATE INDEX idx_venta_estado_fecha     ON venta(estado, fecha_hora);
CREATE INDEX idx_detalle_bebida         ON detalle_venta(bebida_id);
CREATE INDEX idx_pago_tipo_fecha        ON pago(tipo_pago, fecha_pago);
CREATE INDEX idx_mov_producto_fecha     ON movimiento_inventario(producto_id, fecha_movimiento);
CREATE INDEX idx_mov_fecha              ON movimiento_inventario(fecha_movimiento);
CREATE INDEX idx_visita_cliente_fecha   ON visita_cliente(cliente_fidelidad_id, fecha_visita);
