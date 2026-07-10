-- =====================================================
-- AutoPartes CR - Script SQL del modulo de Adrian
-- (Usuarios, Roles, Clientes, Inventario)
-- =====================================================

CREATE DATABASE IF NOT EXISTS autopartes_cr;
USE autopartes_cr;

CREATE USER IF NOT EXISTS 'autopartes_user'@'localhost' IDENTIFIED BY 'autopartes123';
GRANT ALL PRIVILEGES ON autopartes_cr.* TO 'autopartes_user'@'localhost';
FLUSH PRIVILEGES;

-- Tabla de roles
CREATE TABLE IF NOT EXISTS rol (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE
);

INSERT INTO rol (nombre) VALUES
    ('ADMINISTRADOR'),
    ('CLIENTE'),
    ('ENCARGADO_VENTAS');

-- Tabla de usuarios
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
VALUES ('Administrador', 'admin@autopartescr.com', 'admin123',
        (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR'));

-- Tabla de clientes (datos adicionales de un usuario con rol CLIENTE)
CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    usuario_id INT NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- =====================================================
-- NOTA IMPORTANTE PARA LA INTEGRACION:
-- La tabla "repuesto" pertenece al modulo de Eduardo
-- (Catalogo de Repuestos). Aqui se crea una version
-- minima SOLO para poder probar este modulo de forma
-- independiente. Al integrar el proyecto completo,
-- se debe usar la tabla "repuesto" real (con mas
-- columnas: descripcion, precio, marca_id, etc.) y
-- eliminar este CREATE TABLE duplicado.
-- =====================================================
CREATE TABLE IF NOT EXISTS repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

INSERT INTO repuesto (nombre) VALUES
    ('Pastilla de freno delantera'),
    ('Filtro de aceite'),
    ('Bateria 12V 45Ah');

-- Tabla de inventario (HU-07 actualizar stock, HU-08 reporte)
CREATE TABLE IF NOT EXISTS inventario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    repuesto_id INT NOT NULL UNIQUE,
    cantidad_actual INT NOT NULL DEFAULT 0,
    cantidad_minima INT NOT NULL DEFAULT 0,
    FOREIGN KEY (repuesto_id) REFERENCES repuesto(id)
);

INSERT INTO inventario (repuesto_id, cantidad_actual, cantidad_minima)
VALUES
    (1, 3, 10),   -- stock bajo a proposito, para ver la alerta
    (2, 25, 5),
    (3, 8, 4);

/*==============================================================*/
/* MÓDULO DE PEDIDOS - ERICK                                    */
/*==============================================================*/

CREATE TABLE estado_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,

    fecha DATETIME NOT NULL,

    total DECIMAL(10,2) NOT NULL,

    cliente_id INT NOT NULL,

    estado_pedido_id INT NOT NULL,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id),

    CONSTRAINT fk_pedido_estado
        FOREIGN KEY (estado_pedido_id)
        REFERENCES estado_pedido(id)
);

CREATE TABLE detalle_pedido (

    id INT AUTO_INCREMENT PRIMARY KEY,

    pedido_id INT NOT NULL,

    repuesto_id INT NOT NULL,

    cantidad INT NOT NULL,

    precio_unitario DECIMAL(10,2) NOT NULL,

    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedido(id),

    CONSTRAINT fk_detalle_repuesto
        FOREIGN KEY (repuesto_id)
        REFERENCES repuesto(id)
);

/*====================*/
/* ESTADOS INICIALES  */
/*====================*/

INSERT INTO estado_pedido(nombre)
VALUES
('Pendiente'),
('En proceso'),
('Entregado'),
('Cancelado');