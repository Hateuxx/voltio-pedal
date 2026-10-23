-- =====================================================================
--  Voltio & Pedal - Sistema de control de inventario
--  V1__esquema_inicial.sql  (MySQL 8.0.16+)
--  18 tablas, en orden de dependencias (cada FK apunta a una tabla ya creada)
-- =====================================================================
-- ---------------------------------------------------------------------
-- 1. proveedor
-- ---------------------------------------------------------------------
CREATE TABLE proveedor (
  id_proveedor       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  ruc                CHAR(11)     NOT NULL,
  razon_social       VARCHAR(255) NOT NULL,
  contacto           VARCHAR(100) NULL,
  telefono           VARCHAR(20)  NULL,
  rubro              VARCHAR(100) NULL,
  frecuencia_entrega VARCHAR(20)  NOT NULL,
  activo             TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT uq_proveedor_ruc        UNIQUE (ruc),
  CONSTRAINT ck_proveedor_ruc        CHECK (ruc REGEXP '^[0-9]{11}$'),
  CONSTRAINT ck_proveedor_frecuencia CHECK (frecuencia_entrega IN ('SEMANAL','QUINCENAL'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. tipo_repuesto
-- ---------------------------------------------------------------------
CREATE TABLE tipo_repuesto (
  id_tipo_repuesto INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  CONSTRAINT uq_tipo_repuesto_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. cliente
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
  id_cliente       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  tipo_documento   VARCHAR(20)  NOT NULL,
  numero_documento VARCHAR(20)  NOT NULL,
  nombres          VARCHAR(100) NOT NULL,      -- o razón social si es RUC
  apellidos        VARCHAR(100) NULL,
  telefono         VARCHAR(20)  NULL,
  CONSTRAINT uq_cliente_documento UNIQUE (tipo_documento, numero_documento),
  CONSTRAINT ck_cliente_tipo_doc  CHECK (tipo_documento IN ('DNI','CE','PASAPORTE','RUC')),
  CONSTRAINT ck_cliente_dni       CHECK (tipo_documento <> 'DNI' OR numero_documento REGEXP '^[0-9]{8}$'),
  CONSTRAINT ck_cliente_ruc       CHECK (tipo_documento <> 'RUC' OR numero_documento REGEXP '^[0-9]{11}$'),
  CONSTRAINT ck_cliente_apellidos CHECK (tipo_documento = 'RUC' OR apellidos IS NOT NULL)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. usuario  (RF1, RF2, RF3 con pregunta de seguridad, RF18)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  id_usuario           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  tipo_documento       VARCHAR(20)  NOT NULL,
  numero_documento     VARCHAR(20)  NOT NULL,
  nombres              VARCHAR(100) NOT NULL,
  apellidos            VARCHAR(100) NOT NULL,
  contrasena           CHAR(60)     NOT NULL,             -- BCrypt
  rol                  VARCHAR(20)  NOT NULL,
  pregunta_seguridad   VARCHAR(150) NOT NULL,
  respuesta_seguridad  CHAR(60)     NOT NULL,             -- BCrypt de la respuesta normalizada
  intentos_fallidos    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  bloqueado_hasta      DATETIME     NULL,
  activo               TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT uq_usuario_documento UNIQUE (tipo_documento, numero_documento),
  CONSTRAINT ck_usuario_tipo_doc  CHECK (tipo_documento IN ('DNI','CE','PASAPORTE')),
  CONSTRAINT ck_usuario_dni       CHECK (tipo_documento <> 'DNI' OR numero_documento REGEXP '^[0-9]{8}$'),
  CONSTRAINT ck_usuario_rol       CHECK (rol IN ('ADMINISTRADOR','OPERADOR_TIENDA','TECNICO'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. mercaderia  (supertipo; cada fila es un SKU del catálogo)
-- ---------------------------------------------------------------------
CREATE TABLE mercaderia (
  id_mercaderia  INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
  tipo           VARCHAR(20)   NOT NULL,                       -- discriminador JPA
  sku            VARCHAR(50)   NOT NULL,
  descripcion    VARCHAR(255)  NOT NULL,
  stock          INT           NOT NULL DEFAULT 0,
  stock_minimo   INT           NOT NULL DEFAULT 0,
  precio_costo   DECIMAL(12,4) NOT NULL,                       -- CAMBIO 1: DECIMAL(12,4)
  precio_venta   DECIMAL(10,2) NOT NULL,
  fecha_ingreso  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo         TINYINT(1)    NOT NULL DEFAULT 1,
  id_proveedor   INT UNSIGNED  NOT NULL,
  CONSTRAINT uq_mercaderia_sku     UNIQUE (sku),
  CONSTRAINT ck_mercaderia_tipo    CHECK (tipo IN ('SCOOTER','REPUESTO','ACCESORIO')),
  CONSTRAINT ck_mercaderia_stock   CHECK (stock >= 0),
  CONSTRAINT ck_mercaderia_minimo  CHECK (stock_minimo >= 0),
  CONSTRAINT ck_mercaderia_precios CHECK (precio_costo >= 0 AND precio_venta >= 0),
  CONSTRAINT fk_mercaderia_proveedor FOREIGN KEY (id_proveedor)
      REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT,
  INDEX idx_mercaderia_activo_stock (activo, stock),          -- KPIs
  INDEX idx_mercaderia_tipo (tipo, activo)                    -- reportes por categoría
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. scooter  (subtipo)
-- ---------------------------------------------------------------------
CREATE TABLE scooter (
  id_mercaderia  INT UNSIGNED PRIMARY KEY,
  autonomia_km   INT         NOT NULL,
  potencia_watts INT         NOT NULL,
  color          VARCHAR(50) NOT NULL,
  meses_garantia INT         NOT NULL,
  CONSTRAINT ck_scooter_autonomia CHECK (autonomia_km > 0),
  CONSTRAINT ck_scooter_potencia  CHECK (potencia_watts > 0),
  CONSTRAINT ck_scooter_garantia  CHECK (meses_garantia >= 0),
  CONSTRAINT fk_scooter_mercaderia FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. repuesto  (subtipo)
-- ---------------------------------------------------------------------
CREATE TABLE repuesto (
  id_mercaderia       INT UNSIGNED PRIMARY KEY,
  id_tipo_repuesto    INT UNSIGNED NOT NULL,
  modelos_compatibles VARCHAR(255) NOT NULL,
  CONSTRAINT fk_repuesto_mercaderia FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE CASCADE,
  CONSTRAINT fk_repuesto_tipo FOREIGN KEY (id_tipo_repuesto)
      REFERENCES tipo_repuesto(id_tipo_repuesto) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. accesorio  (subtipo)
-- ---------------------------------------------------------------------
CREATE TABLE accesorio (
  id_mercaderia INT UNSIGNED PRIMARY KEY,
  talla         VARCHAR(20) NULL,
  color         VARCHAR(50) NULL,
  CONSTRAINT fk_accesorio_mercaderia FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. venta  (RF11: recibida del sistema de ventas externo)
-- ---------------------------------------------------------------------
CREATE TABLE venta (
  id_venta           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_cliente         INT UNSIGNED  NULL,          -- NULL = venta al paso
  id_usuario         INT UNSIGNED  NOT NULL,      -- cajero
  tipo_comprobante   VARCHAR(20)   NOT NULL,
  numero_comprobante VARCHAR(20)   NOT NULL,      -- B001-0045
  fecha_hora         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total              DECIMAL(10,2) NOT NULL,
  activo             TINYINT(1)    NOT NULL DEFAULT 1,
  CONSTRAINT uq_venta_comprobante UNIQUE (tipo_comprobante, numero_comprobante),  -- idempotencia
  CONSTRAINT ck_venta_tipo        CHECK (tipo_comprobante IN ('BOLETA','FACTURA')),
  CONSTRAINT ck_venta_factura     CHECK (tipo_comprobante <> 'FACTURA' OR id_cliente IS NOT NULL),
  CONSTRAINT ck_venta_total       CHECK (total >= 0),
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
  CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
  INDEX idx_venta_fecha (fecha_hora)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. detalle_venta
-- ---------------------------------------------------------------------
CREATE TABLE detalle_venta (
  id_detalle_venta INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_venta         INT UNSIGNED  NOT NULL,
  id_mercaderia    INT UNSIGNED  NOT NULL,
  cantidad         INT           NOT NULL,
  precio_unitario  DECIMAL(10,2) NOT NULL,
  costo_unitario   DECIMAL(12,4) NOT NULL,                     -- CAMBIO 2: Añadido costo unitario histórico
  numero_serie     VARCHAR(50)   NULL,
  CONSTRAINT uq_detalle_venta_serie  UNIQUE (numero_serie),
  CONSTRAINT ck_detalle_venta_cant   CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_venta_serie  CHECK (numero_serie IS NULL OR cantidad = 1),
  CONSTRAINT ck_detalle_venta_precio CHECK (precio_unitario >= 0),
  CONSTRAINT ck_detalle_venta_costo  CHECK (costo_unitario >= 0), -- CAMBIO 2: Check agregado
  CONSTRAINT fk_detalle_venta_venta FOREIGN KEY (id_venta)
      REFERENCES venta(id_venta) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_venta_mercaderia FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11. servicio_tecnico  (RF10)
-- ---------------------------------------------------------------------
CREATE TABLE servicio_tecnico (
  id_servicio_tecnico   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  fecha_ingreso         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_entrega         DATETIME     NULL,
  numero_serie          VARCHAR(50)  NOT NULL,
  falla_reportada       TEXT         NOT NULL,
  diagnostico           TEXT         NULL,
  estado                VARCHAR(50)  NOT NULL DEFAULT 'INGRESADO',
  fecha_compra          DATE         NULL,     -- desde la venta o ingresada a mano
  meses_garantia        INT          NULL,
  cubierto_por_garantia TINYINT(1)   NOT NULL DEFAULT 0,
  id_cliente            INT UNSIGNED NOT NULL,
  id_scooter            INT UNSIGNED NOT NULL,
  id_tecnico            INT UNSIGNED NULL,
  CONSTRAINT ck_servicio_estado CHECK (estado IN ('INGRESADO','EN_REPARACION',
      'ESPERANDO_REPUESTO','LISTO_PARA_ENTREGA','ENTREGADO')),
  CONSTRAINT ck_servicio_fechas    CHECK (fecha_entrega IS NULL OR fecha_entrega >= fecha_ingreso),
  CONSTRAINT ck_servicio_entregado CHECK ((estado = 'ENTREGADO') = (fecha_entrega IS NOT NULL)),
  CONSTRAINT ck_servicio_compra    CHECK (fecha_compra IS NULL OR fecha_compra <= fecha_ingreso),
  CONSTRAINT ck_servicio_meses     CHECK (meses_garantia IS NULL OR meses_garantia >= 0),
  CONSTRAINT ck_servicio_garantia  CHECK (cubierto_por_garantia = 0 OR
      (fecha_compra IS NOT NULL AND meses_garantia IS NOT NULL)),
  CONSTRAINT fk_servicio_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)    ON DELETE RESTRICT,
  CONSTRAINT fk_servicio_scooter FOREIGN KEY (id_scooter) REFERENCES scooter(id_mercaderia) ON DELETE RESTRICT,
  CONSTRAINT fk_servicio_tecnico FOREIGN KEY (id_tecnico) REFERENCES usuario(id_usuario)    ON DELETE RESTRICT,
  INDEX idx_servicio_serie (numero_serie),
  INDEX idx_servicio_tecnico_estado (id_tecnico, estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 12. detalle_servicio  (RF17: consumo de repuestos)
-- ---------------------------------------------------------------------
CREATE TABLE detalle_servicio (
  id_detalle_servicio INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_servicio_tecnico INT UNSIGNED  NOT NULL,
  id_repuesto         INT UNSIGNED  NOT NULL,
  cantidad            INT           NOT NULL,
  precio_unitario     DECIMAL(10,2) NOT NULL,
  costo_unitario      DECIMAL(12,4) NOT NULL,                     -- CAMBIO 3: Añadido costo unitario histórico
  fecha_hora          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_detalle_srv_cant   CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_srv_precio CHECK (precio_unitario >= 0),
  CONSTRAINT ck_detalle_srv_costo  CHECK (costo_unitario >= 0),   -- CAMBIO 3: Check agregado
  CONSTRAINT fk_detalle_srv_cabecera FOREIGN KEY (id_servicio_tecnico)
      REFERENCES servicio_tecnico(id_servicio_tecnico) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_srv_repuesto FOREIGN KEY (id_repuesto)
      REFERENCES repuesto(id_mercaderia) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 13. ingreso_mercaderia  (cabecera: guía de remisión)
-- ---------------------------------------------------------------------
CREATE TABLE ingreso_mercaderia (
  id_ingreso    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_proveedor  INT UNSIGNED NOT NULL,
  numero_guia   VARCHAR(20)  NOT NULL,         -- p. ej. T001-00004521
  fecha_hora    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_usuario    INT UNSIGNED NOT NULL,         -- encargado que recibió
  observacion   VARCHAR(255) NULL,
  activo        TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT uq_ingreso_guia UNIQUE (id_proveedor, numero_guia),
  CONSTRAINT fk_ingreso_proveedor FOREIGN KEY (id_proveedor)
      REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT,
  CONSTRAINT fk_ingreso_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 14. detalle_ingreso
-- ---------------------------------------------------------------------
CREATE TABLE detalle_ingreso (
  id_detalle_ingreso INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_ingreso         INT UNSIGNED  NOT NULL,
  id_mercaderia      INT UNSIGNED  NOT NULL,
  cantidad           INT           NOT NULL,
  costo_unitario     DECIMAL(12,4) NOT NULL,                     -- CAMBIO 4: DECIMAL(12,4)
  CONSTRAINT uq_detalle_ingreso       UNIQUE (id_ingreso, id_mercaderia),
  CONSTRAINT ck_detalle_ingreso_cant  CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_ingreso_costo CHECK (costo_unitario >= 0),
  CONSTRAINT fk_detalle_ingreso_cab FOREIGN KEY (id_ingreso)
      REFERENCES ingreso_mercaderia(id_ingreso) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_ingreso_merc FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 15. conteo_inventario  (indicador de exactitud de inventario)
-- ---------------------------------------------------------------------
CREATE TABLE conteo_inventario (
  id_conteo     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  fecha_inicio  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_cierre  DATETIME     NULL,
  estado        VARCHAR(20)  NOT NULL DEFAULT 'ABIERTO',
  id_usuario    INT UNSIGNED NOT NULL,
  observacion   VARCHAR(255) NULL,
  CONSTRAINT ck_conteo_estado CHECK (estado IN ('ABIERTO','CERRADO')),
  CONSTRAINT ck_conteo_fechas CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_inicio),
  CONSTRAINT fk_conteo_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 16. detalle_conteo
-- ---------------------------------------------------------------------
CREATE TABLE detalle_conteo (
  id_detalle_conteo INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_conteo      INT UNSIGNED NOT NULL,
  id_mercaderia  INT UNSIGNED NOT NULL,
  stock_sistema  INT NOT NULL,        -- foto del stock al momento de contar
  stock_fisico   INT NOT NULL,
  diferencia     INT AS (stock_fisico - stock_sistema) STORED,
  ajustado       TINYINT(1) NOT NULL DEFAULT 0,
  CONSTRAINT uq_detalle_conteo       UNIQUE (id_conteo, id_mercaderia),
  CONSTRAINT ck_detalle_conteo_stock CHECK (stock_sistema >= 0 AND stock_fisico >= 0),
  CONSTRAINT fk_detalle_conteo_cab FOREIGN KEY (id_conteo)
      REFERENCES conteo_inventario(id_conteo) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_conteo_merc FOREIGN KEY (id_mercaderia)
      REFERENCES mercaderia(id_mercaderia) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 17. movimiento_inventario  (kardex, núcleo de RF9 y RF14)
-- ---------------------------------------------------------------------
CREATE TABLE movimiento_inventario (
  id_movimiento       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  fecha_hora          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo                VARCHAR(50)  NOT NULL,
  cantidad            INT          NOT NULL,
  stock_anterior      INT          NOT NULL,
  stock_resultante    INT          NOT NULL,
  id_mercaderia       INT UNSIGNED NOT NULL,
  id_usuario          INT UNSIGNED NOT NULL,
  id_venta            INT UNSIGNED NULL,
  id_servicio_tecnico INT UNSIGNED NULL,
  id_ingreso          INT UNSIGNED NULL,
  id_conteo           INT UNSIGNED NULL,
  observacion         VARCHAR(255) NULL,

  CONSTRAINT ck_mov_tipo CHECK (tipo IN (
      'ENTRADA_PROVEEDOR','DEVOLUCION_PROVEEDOR',
      'SALIDA_VENTA','DEVOLUCION_VENTA',
      'SALIDA_REPARACION','DEVOLUCION_REPARACION',
      'AJUSTE_ENTRADA','AJUSTE_SALIDA')),
  CONSTRAINT ck_mov_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_mov_saldo    CHECK (stock_anterior >= 0 AND stock_resultante >= 0),
  CONSTRAINT ck_mov_aritmetica CHECK (stock_resultante = stock_anterior +
      CASE WHEN tipo IN ('ENTRADA_PROVEEDOR','DEVOLUCION_VENTA',
                         'DEVOLUCION_REPARACION','AJUSTE_ENTRADA')
           THEN cantidad ELSE -cantidad END),
  CONSTRAINT ck_mov_origen_venta    CHECK ((id_venta IS NOT NULL) =
      (tipo IN ('SALIDA_VENTA','DEVOLUCION_VENTA'))),
  CONSTRAINT ck_mov_origen_servicio CHECK ((id_servicio_tecnico IS NOT NULL) =
      (tipo IN ('SALIDA_REPARACION','DEVOLUCION_REPARACION'))),
  CONSTRAINT ck_mov_origen_ingreso  CHECK ((id_ingreso IS NOT NULL) =
      (tipo IN ('ENTRADA_PROVEEDOR','DEVOLUCION_PROVEEDOR'))),
  CONSTRAINT ck_mov_origen_conteo   CHECK (id_conteo IS NULL OR
      tipo IN ('AJUSTE_ENTRADA','AJUSTE_SALIDA')),
  CONSTRAINT ck_mov_ajuste_motivo   CHECK (tipo NOT LIKE 'AJUSTE%' OR observacion IS NOT NULL),

  CONSTRAINT fk_mov_mercaderia FOREIGN KEY (id_mercaderia)       REFERENCES mercaderia(id_mercaderia)             ON DELETE RESTRICT,
  CONSTRAINT fk_mov_usuario    FOREIGN KEY (id_usuario)          REFERENCES usuario(id_usuario)                   ON DELETE RESTRICT,
  CONSTRAINT fk_mov_venta      FOREIGN KEY (id_venta)            REFERENCES venta(id_venta)                       ON DELETE RESTRICT,
  CONSTRAINT fk_mov_servicio   FOREIGN KEY (id_servicio_tecnico) REFERENCES servicio_tecnico(id_servicio_tecnico) ON DELETE RESTRICT,
  CONSTRAINT fk_mov_ingreso    FOREIGN KEY (id_ingreso)          REFERENCES ingreso_mercaderia(id_ingreso)        ON DELETE RESTRICT,
  CONSTRAINT fk_mov_conteo     FOREIGN KEY (id_conteo)           REFERENCES conteo_inventario(id_conteo)          ON DELETE RESTRICT,

  INDEX idx_mov_kardex  (id_mercaderia, fecha_hora),
  INDEX idx_mov_reporte (fecha_hora, tipo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 18. registro_auditoria  (RF15; id_registro sin FK para sobrevivir a RF8)
-- ---------------------------------------------------------------------
CREATE TABLE registro_auditoria (
  id_registro_auditoria INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  fecha_hora            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_usuario            INT UNSIGNED NOT NULL,
  accion                VARCHAR(50)  NOT NULL,
  entidad_afectada      VARCHAR(100) NOT NULL,
  id_registro           VARCHAR(50)  NOT NULL,     -- código legible: SKU, n.º comprobante
  motivo                TEXT         NOT NULL,
  CONSTRAINT ck_auditoria_accion  CHECK (accion IN ('INACTIVACION','REACTIVACION','ELIMINACION_FISICA')),
  CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
  INDEX idx_auditoria_filtro (entidad_afectada, fecha_hora)
) ENGINE=InnoDB;