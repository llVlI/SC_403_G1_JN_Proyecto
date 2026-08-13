-- =========================================================
-- AutoPartes CR - Script SQL final
-- Módulos: Adrian (Usuarios/Roles/Clientes/Inventario)
--          Eduardo (Catálogo: Marca/Categoria/Repuesto)
--          Erick   (Pedidos/Detalle Pedido/Estado Pedido)
--
CREATE DATABASE IF NOT EXISTS autopartes_cr;
USE autopartes_cr;

-- Usuario de aplicación (solo entorno local/desarrollo).
-- En producción, gestionar credenciales fuera del script (env vars / secret manager).
CREATE USER IF NOT EXISTS 'autopartes_user'@'localhost' IDENTIFIED BY 'autopartes123';
GRANT ALL PRIVILEGES ON autopartes_cr.* TO 'autopartes_user'@'localhost';
FLUSH PRIVILEGES;

-- ---------------------------------------------------------
-- Módulo de Adrian (Usuarios, Roles, Clientes)
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS rol (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE
);

INSERT INTO rol (nombre)
SELECT * FROM (SELECT 'ADMINISTRADOR' AS nombre) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMINISTRADOR');

INSERT INTO rol (nombre)
SELECT * FROM (SELECT 'CLIENTE' AS nombre) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'CLIENTE');

INSERT INTO rol (nombre)
SELECT * FROM (SELECT 'ENCARGADO_VENTAS' AS nombre) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ENCARGADO_VENTAS');

CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    rol_id INT NOT NULL,
    FOREIGN KEY (rol_id) REFERENCES rol(id)
);

-- Usuario administrador de prueba (correo: admin@autopartescr.com / clave: admin123)
INSERT INTO usuario (nombre, email, password, rol_id)
SELECT 'Administrador', 'admin@autopartescr.com', 'admin123',
       (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin@autopartescr.com');

CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    usuario_id INT NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ---------------------------------------------------------
-- Módulo de Eduardo (Catálogo de Repuestos)
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS marca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

INSERT IGNORE INTO marca (nombre) VALUES
    ('Toyota'),
    ('Hyundai'),
    ('Nissan');

CREATE TABLE IF NOT EXISTS categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

INSERT IGNORE INTO categoria (nombre) VALUES
    ('Frenos'),
    ('Motor'),
    ('Electrico');

CREATE TABLE IF NOT EXISTS repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    marca_id INT NOT NULL,
    categoria_id INT NOT NULL,
    FOREIGN KEY (marca_id) REFERENCES marca(id),
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

-- codigo es UNIQUE, así que INSERT IGNORE ya previene duplicados aquí
INSERT IGNORE INTO repuesto (nombre, codigo, descripcion, precio, stock, marca_id, categoria_id) VALUES
    ('Pastilla de freno delantera', 'PF-001', 'Pastillas de freno delanteras', 18500.00, 3,
        (SELECT id FROM marca WHERE nombre = 'Toyota'), (SELECT id FROM categoria WHERE nombre = 'Frenos')),
    ('Filtro de aceite', 'FA-002', 'Filtro de aceite de motor', 6500.00, 25,
        (SELECT id FROM marca WHERE nombre = 'Hyundai'), (SELECT id FROM categoria WHERE nombre = 'Motor')),
    ('Bateria 12V 45Ah', 'BAT-003', 'Bateria 12V 45Ah libre de mantenimiento', 65000.00, 8,
        (SELECT id FROM marca WHERE nombre = 'Nissan'), (SELECT id FROM categoria WHERE nombre = 'Electrico'));

-- Tabla de inventario (HU-07 actualizar stock, HU-08 reporte)
CREATE TABLE IF NOT EXISTS inventario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    repuesto_id INT NOT NULL UNIQUE,
    cantidad_actual INT NOT NULL DEFAULT 0,
    cantidad_minima INT NOT NULL DEFAULT 0,
    FOREIGN KEY (repuesto_id) REFERENCES repuesto(id)
);

-- repuesto_id es UNIQUE, así que INSERT IGNORE previene duplicados
INSERT IGNORE INTO inventario (repuesto_id, cantidad_actual, cantidad_minima) VALUES
    ((SELECT id FROM repuesto WHERE codigo = 'PF-001'), 3, 10),   -- stock bajo a propósito, para ver la alerta
    ((SELECT id FROM repuesto WHERE codigo = 'FA-002'), 25, 5),
    ((SELECT id FROM repuesto WHERE codigo = 'BAT-003'), 8, 4);

-- ---------------------------------------------------------
-- Módulo de Erick (Pedidos)
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS estado_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    cliente_id INT NOT NULL,
    estado_pedido_id INT NOT NULL,
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_estado
        FOREIGN KEY (estado_pedido_id) REFERENCES estado_pedido(id)
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT NOT NULL,
    repuesto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_detalle_repuesto
        FOREIGN KEY (repuesto_id) REFERENCES repuesto(id)
);

-- Estados iniciales (los 5 del flujo de pedido).
-- nombre es UNIQUE, INSERT IGNORE evita duplicados en reejecuciones.
-- "Listo para entrega" YA estaba aquí, pero faltaba en el <select>
-- de pedidos/gestionPedidos.html (corregido aparte).
INSERT IGNORE INTO estado_pedido (nombre) VALUES
    ('Pendiente'),
    ('En proceso'),
    ('Listo para entrega'),
    ('Entregado'),
    ('Cancelado');
