-- ============================================================
-- V1__baseline.sql
-- Baseline de MunaqFit: espejo fiel del esquema auditado de 14 tablas
-- (antes database/schema.sql). Aqui la tabla producto TODAVIA tiene
-- proveedor_id; la relacion N-N llega en V2.
--
-- Convenciones (heredadas de la auditoria):
--   * Toda fecha/hora es DATETIME (TIMESTAMP termina en 2038 y se corre
--     solo con la zona horaria del servidor).
--   * Las reglas que caben en una sola tabla se aplican con CHECK.
--   * Las referencias entre dominios distintos usan claves foraneas.
-- ============================================================

-- ============================================================
-- PARAMETRO
-- ============================================================
CREATE TABLE parametro (
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
CREATE TABLE usuario (
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
-- CATEGORIA (clasificacion de INSUMOS)
-- ============================================================
CREATE TABLE categoria (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT        NULL,
  CONSTRAINT chk_categoria_nombre CHECK (nombre <> '')
) ENGINE=InnoDB;

-- ============================================================
-- CATEGORIA_BEBIDA (clasificacion de BEBIDAS por beneficio)
-- ============================================================
CREATE TABLE categoria_bebida (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT        NULL,
  CONSTRAINT chk_categoria_bebida_nombre CHECK (nombre <> '')
) ENGINE=InnoDB;

-- ============================================================
-- PROVEEDOR
-- ============================================================
CREATE TABLE proveedor (
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
-- proveedor_id existe en V1 y se elimina en V2, cuando la relacion
-- muchos-a-muchos producto<->proveedor quede en producto_proveedor.
-- stock_actual es cache: la fuente de verdad es el kardex.
-- ============================================================
CREATE TABLE producto (
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
-- ============================================================
CREATE TABLE bebida (
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
-- la cantidad va en la unidad canonica del producto; no existe
-- columna 'unidad' para no obligar a convertir G<->KG / ML<->L.
-- ============================================================
CREATE TABLE receta (
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
-- subtotal/igv/total/igv_tasa son instantaneas para que un reporte
-- historico siga siendo correcto aunque la tasa cambie.
-- ============================================================
CREATE TABLE venta (
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
-- ============================================================
CREATE TABLE detalle_venta (
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
-- no se guarda monto_total: se deriva de venta.total (3FN).
-- ============================================================
CREATE TABLE pago (
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
-- referencia_id es polimorfica (sin FK), pero tipo_referencia la
-- hace interpretable y el CHECK obliga a que vayan juntos o no vayan.
-- ============================================================
CREATE TABLE movimiento_inventario (
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
-- ============================================================
CREATE TABLE cliente_fidelidad (
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
-- ============================================================
CREATE TABLE visita_cliente (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_fidelidad_id BIGINT NOT NULL,
  venta_id             BIGINT NULL,
  fecha_visita         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_visita_cliente FOREIGN KEY (cliente_fidelidad_id) REFERENCES cliente_fidelidad(id),
  CONSTRAINT fk_visita_venta   FOREIGN KEY (venta_id) REFERENCES venta(id),
  CONSTRAINT uq_visita_venta   UNIQUE (venta_id)
) ENGINE=InnoDB;

-- ============================================================
-- INDICES (los de las FK los crea InnoDB solo)
-- ============================================================
CREATE INDEX idx_producto_nombre      ON producto(nombre);
CREATE INDEX idx_producto_categoria   ON producto(categoria_id, nombre);
CREATE INDEX idx_bebida_nombre        ON bebida(nombre);
CREATE INDEX idx_bebida_categoria     ON bebida(categoria_id);
CREATE INDEX idx_receta_producto      ON receta(producto_id);
CREATE INDEX idx_venta_fecha          ON venta(fecha_hora);
CREATE INDEX idx_venta_estado_fecha   ON venta(estado, fecha_hora);
CREATE INDEX idx_detalle_bebida       ON detalle_venta(bebida_id);
CREATE INDEX idx_pago_tipo_fecha      ON pago(tipo_pago, fecha_pago);
CREATE INDEX idx_mov_producto_fecha   ON movimiento_inventario(producto_id, fecha_movimiento);
CREATE INDEX idx_mov_fecha            ON movimiento_inventario(fecha_movimiento);
CREATE INDEX idx_visita_cliente_fecha ON visita_cliente(cliente_fidelidad_id, fecha_visita);