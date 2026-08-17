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
-- La contrasena se guarda con BCrypt (hash de "admin123"), requerido por
-- Spring Security. Nunca se guardan contrasenas en texto plano.
INSERT INTO usuario (nombre, email, password, rol_id)
SELECT 'Administrador', 'admin@autopartescr.com', '$2a$10$.fJmGN1IiM5zv63H/U428.cXH0VUBe.jhRZ4jEXCXwawvoT1CdO.2',
       (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'admin@autopartescr.com');

-- Usuario cliente de prueba (correo: cliente@autopartescr.com / clave: cliente123)
INSERT INTO usuario (nombre, email, password, rol_id)
SELECT 'Cliente Demo', 'cliente@autopartescr.com', '$2a$10$A7xVup2l283Hi0qZme0qxupPRnhY0icl3LKNKwi2kZGnvMVrJ8UXK',
       (SELECT id FROM rol WHERE nombre = 'CLIENTE')
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'cliente@autopartescr.com');

-- Usuario encargado de ventas de prueba (correo: ventas@autopartescr.com / clave: ventas123)
INSERT INTO usuario (nombre, email, password, rol_id)
SELECT 'Encargado Ventas', 'ventas@autopartescr.com', '$2a$10$OW2jtuHGLRafG4JRU6P.x.Hrej3UjzL/.pK4wtzQeOYrSDsm.Ev0K',
       (SELECT id FROM rol WHERE nombre = 'ENCARGADO_VENTAS')
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'ventas@autopartescr.com');

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

-- ---------------------------------------------------------
-- Modulo de seguridad (Spring Security)
-- ---------------------------------------------------------
-- Tabla que guarda las rutas protegidas de la aplicacion. En vez de
-- escribir las reglas de acceso directamente en el codigo Java, se
-- guardan aqui: SecurityConfig las lee al arrancar la aplicacion.
--
-- requiere_rol = 0  -> ruta publica (no necesita login)
-- requiere_rol = 1  -> ruta protegida (necesita el rol de la columna rol_id)
--
-- ADMINISTRADOR hereda automaticamente los permisos de ENCARGADO_VENTAS
-- (ver roleHierarchy en SecurityConfig), asi que no hace falta duplicar
-- las rutas de pedidos para ambos roles.
CREATE TABLE IF NOT EXISTS ruta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ruta VARCHAR(100) NOT NULL,
    requiere_rol BOOLEAN NOT NULL DEFAULT FALSE,
    rol_id INT,
    FOREIGN KEY (rol_id) REFERENCES rol(id)
);

-- Rutas publicas: catalogo, login, registro, pagina de inicio.
INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/' AS ruta, FALSE AS requiere_rol, NULL AS rol_id) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/catalogo' AS ruta, FALSE, NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/catalogo');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/catalogo/**' AS ruta, FALSE, NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/catalogo/**');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/login' AS ruta, FALSE, NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/login');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/registro' AS ruta, FALSE, NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/registro');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT * FROM (SELECT '/registro/**' AS ruta, FALSE, NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/registro/**');

-- Rutas solo para ADMINISTRADOR: gestion de inventario y catalogo.
INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/inventario/**', TRUE, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/inventario/**');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/repuestos/**', TRUE, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/repuestos/**');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/categorias/**', TRUE, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/categorias/**');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/marcas/**', TRUE, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/marcas/**');

-- Rutas para ENCARGADO_VENTAS (y ADMINISTRADOR, que hereda el permiso):
-- gestion de pedidos.
INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/pedidos/gestion', TRUE, (SELECT id FROM rol WHERE nombre = 'ENCARGADO_VENTAS')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/pedidos/gestion');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/pedidos/*/estado', TRUE, (SELECT id FROM rol WHERE nombre = 'ENCARGADO_VENTAS')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/pedidos/*/estado');

-- Rutas para CLIENTE: carrito y mis pedidos.
INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/pedidos/carrito', TRUE, (SELECT id FROM rol WHERE nombre = 'CLIENTE')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/pedidos/carrito');

INSERT INTO ruta (ruta, requiere_rol, rol_id)
SELECT '/pedidos/mis-pedidos', TRUE, (SELECT id FROM rol WHERE nombre = 'CLIENTE')
WHERE NOT EXISTS (SELECT 1 FROM ruta WHERE ruta = '/pedidos/mis-pedidos');
