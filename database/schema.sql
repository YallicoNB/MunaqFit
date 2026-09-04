-- ============================================================
-- MUNAQ FIT MANAGER - SCHEMA
-- Motor: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS munaqfit
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE munaqfit;

-- ============================================================
-- USUARIO
-- ============================================================
CREATE TABLE IF NOT EXISTS usuario (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  dni             VARCHAR(20)  NOT NULL UNIQUE,
  nombre_completo VARCHAR(100) NOT NULL,
  email           VARCHAR(100) NOT NULL UNIQUE,
  password        VARCHAR(255) NOT NULL,
  rol             ENUM('ADMIN','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
  estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  ultimo_login    TIMESTAMP NULL,
  fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  intentos_fallidos INT NOT NULL DEFAULT 0,
  bloqueado_hasta  TIMESTAMP NULL
) ENGINE=InnoDB;

-- ============================================================
-- CATEGORIA
-- ============================================================
CREATE TABLE IF NOT EXISTS categoria (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(50) NOT NULL,
  descripcion TEXT NULL
) ENGINE=InnoDB;

-- ============================================================
-- PROVEEDOR
-- ============================================================
CREATE TABLE IF NOT EXISTS proveedor (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre          VARCHAR(100) NOT NULL,
  ruc             VARCHAR(20) NULL,
  telefono        VARCHAR(20) NULL,
  direccion       VARCHAR(200) NULL,
  contacto_nombre VARCHAR(100) NULL,
  estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  tipo_contrato   ENUM('FIJO','VARIABLE') NULL
) ENGINE=InnoDB;

-- ============================================================
-- PRODUCTO (Insumo)
-- ============================================================
CREATE TABLE IF NOT EXISTS producto (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre          VARCHAR(100) NOT NULL,
  categoria_id    BIGINT NULL,
  proveedor_id    BIGINT NULL,
  stock_actual    DECIMAL(10,2) NOT NULL DEFAULT 0,
  stock_minimo    DECIMAL(10,2) NOT NULL DEFAULT 0,
  stock_critico   DECIMAL(10,2) NOT NULL DEFAULT 0,
  unidad_medida   ENUM('KG','G','L','ML','UNIDAD') NOT NULL DEFAULT 'UNIDAD',
  costo_unitario  DECIMAL(10,2) NOT NULL DEFAULT 0,
  precio_venta    DECIMAL(10,2) NULL,
  fecha_caducidad DATE NULL,
  ultima_reposicion TIMESTAMP NULL,
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id)
) ENGINE=InnoDB;

-- ============================================================
-- BEBIDA
-- ============================================================
CREATE TABLE IF NOT EXISTS bebida (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre             VARCHAR(100) NOT NULL,
  descripcion        TEXT NULL,
  precio             DECIMAL(10,2) NOT NULL DEFAULT 16.00,
  categoria          VARCHAR(50) NULL,
  imagen_url         VARCHAR(255) NULL,
  tiempo_preparacion INT NULL,
  activo             BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ============================================================
-- RECETA
-- ============================================================
CREATE TABLE IF NOT EXISTS receta (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  bebida_id     BIGINT NOT NULL,
  producto_id   BIGINT NOT NULL,
  cantidad      DECIMAL(10,2) NOT NULL,
  unidad        VARCHAR(20) NULL,
  paso_instruccion TEXT NULL,
  CONSTRAINT fk_receta_bebida FOREIGN KEY (bebida_id) REFERENCES bebida(id),
  CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB;

-- ============================================================
-- VENTA
-- ============================================================
CREATE TABLE IF NOT EXISTS venta (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id  BIGINT NOT NULL,
  fecha_hora  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  mesa        INT NULL,
  tipo_venta  ENUM('LOCAL','DELIVERY') NOT NULL DEFAULT 'LOCAL',
  subtotal    DECIMAL(10,2) NOT NULL DEFAULT 0,
  igv         DECIMAL(10,2) NOT NULL DEFAULT 0,
  total       DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado      ENUM('PENDIENTE','PAGADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  notas       VARCHAR(255) NULL,
  numero_pedido VARCHAR(30) NULL,
  CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ============================================================
-- DETALLE_VENTA
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_venta (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id       BIGINT NOT NULL,
  bebida_id      BIGINT NOT NULL,
  cantidad       INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal       DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES venta(id),
  CONSTRAINT fk_detalle_bebida FOREIGN KEY (bebida_id) REFERENCES bebida(id)
) ENGINE=InnoDB;

-- ============================================================
-- PAGO
-- ============================================================
CREATE TABLE IF NOT EXISTS pago (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id         BIGINT NOT NULL,
  monto_total      DECIMAL(10,2) NOT NULL,
  monto_pagado     DECIMAL(10,2) NOT NULL,
  monto_cambio     DECIMAL(10,2) NOT NULL DEFAULT 0,
  tipo_pago        ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','QR') NOT NULL,
  numero_operacion VARCHAR(50) NULL,
  banco            VARCHAR(50) NULL,
  fecha_pago       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado           ENUM('PENDIENTE','COMPLETADO') NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id) REFERENCES venta(id)
) ENGINE=InnoDB;

-- ============================================================
-- MOVIMIENTO_INVENTARIO (Kardex)
-- ============================================================
CREATE TABLE IF NOT EXISTS movimiento_inventario (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id      BIGINT NOT NULL,
  tipo_movimiento  ENUM('INGRESO','SALIDA') NOT NULL,
  cantidad         DECIMAL(10,2) NOT NULL,
  stock_anterior   DECIMAL(10,2) NOT NULL,
  stock_nuevo      DECIMAL(10,2) NOT NULL,
  motivo           VARCHAR(100) NULL,
  referencia_id    BIGINT NULL,
  fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id       BIGINT NULL,
  CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
  CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- ============================================================
-- CLIENTE_FIDELIDAD
-- ============================================================
CREATE TABLE IF NOT EXISTS cliente_fidelidad (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  telefono       VARCHAR(20) NULL,
  email          VARCHAR(100) NULL,
  visitas        INT NOT NULL DEFAULT 0,
  umbral_premio  INT NOT NULL DEFAULT 10,
  ultima_visita  TIMESTAMP NULL,
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- VISITA_CLIENTE
-- ============================================================
CREATE TABLE IF NOT EXISTS visita_cliente (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_fidelidad_id BIGINT NOT NULL,
  venta_id            BIGINT NULL,
  fecha_visita        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_visita_cliente FOREIGN KEY (cliente_fidelidad_id) REFERENCES cliente_fidelidad(id),
  CONSTRAINT fk_visita_venta FOREIGN KEY (venta_id) REFERENCES venta(id)
) ENGINE=InnoDB;

CREATE INDEX idx_bebida_categoria ON bebida(categoria);
CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_venta_fecha ON venta(fecha_hora);
CREATE INDEX idx_mov_fecha ON movimiento_inventario(fecha_movimiento);
