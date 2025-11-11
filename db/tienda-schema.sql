-- Esquema base de datos para la tienda de libros
-- Crear tablas
CREATE TABLE TEMA (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  nombre VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE LIBRO (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  titulo VARCHAR(300) NOT NULL,
  isbn VARCHAR(32) NOT NULL UNIQUE,
  descripcion CLOB,
  editorial VARCHAR(200),
  autor VARCHAR(200),
  disponible BOOLEAN NOT NULL,
  precio DECIMAL(12,2) NOT NULL,
  anio INT,
  inventario INT NOT NULL,
  foto VARCHAR(500),
  tema_id BIGINT NOT NULL REFERENCES TEMA(id)
);

CREATE TABLE CLIENTE (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  login VARCHAR(100) NOT NULL UNIQUE,
  nombre VARCHAR(200) NOT NULL,
  mail VARCHAR(200) NOT NULL,
  direccion VARCHAR(400),
  pwd VARCHAR(512) NOT NULL
);

CREATE TABLE GRUPO (
  nombre VARCHAR(100) PRIMARY KEY
);

CREATE TABLE CLIENTE_GRUPO (
  cliente_login VARCHAR(100) NOT NULL REFERENCES CLIENTE(login),
  grupos_nombre VARCHAR(100) NOT NULL REFERENCES GRUPO(nombre),
  PRIMARY KEY (cliente_login, grupos_nombre)
);

CREATE TABLE PEDIDO (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  cliente_id BIGINT NOT NULL REFERENCES CLIENTE(id),
  fecha TIMESTAMP NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  estado VARCHAR(50) NOT NULL
);

CREATE TABLE LIBROVENDIDO (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  pedido_id BIGINT NOT NULL REFERENCES PEDIDO(id),
  libro_id BIGINT NOT NULL REFERENCES LIBRO(id),
  cantidad INT NOT NULL,
  importe DECIMAL(12,2) NOT NULL
);

-- Datos iniciales de catálogo
INSERT INTO TEMA (nombre) VALUES
  ('Ficción'),
  ('Tecnología'),
  ('Historia'),
  ('Infantil');

INSERT INTO LIBRO (titulo, isbn, descripcion, editorial, autor, disponible, precio, anio, inventario, foto, tema_id) VALUES
  ('El viaje interminable', 'ISBN-0001', 'Una aventura épica por mundos imaginarios.', 'Ediciones Fantasía', 'A. Autor', TRUE, 19.95, 2015, 10, NULL, 1),
  ('Misterios del espacio', 'ISBN-0002', 'Descubre los secretos del universo.', 'Cosmos Press', 'B. Científico', TRUE, 24.50, 2019, 8, NULL, 1),
  ('Java EE para desarrolladores', 'ISBN-1001', 'Guía práctica para construir aplicaciones empresariales.', 'TechBooks', 'C. Ingeniero', TRUE, 39.90, 2020, 12, NULL, 2),
  ('Arquitectura de microservicios', 'ISBN-1002', 'Patrones y buenas prácticas modernas.', 'TechBooks', 'D. Arquitecto', TRUE, 34.20, 2021, 15, NULL, 2),
  ('Historia de Roma', 'ISBN-2001', 'Un recorrido completo por la antigua Roma.', 'Historia Viva', 'E. Historiador', TRUE, 27.80, 2012, 9, NULL, 3),
  ('Grandes exploradores', 'ISBN-2002', 'Historias de los exploradores más famosos.', 'Historia Viva', 'F. Cronista', TRUE, 21.60, 2018, 7, NULL, 3),
  ('Cuentos para dormir', 'ISBN-3001', 'Relatos breves para niños antes de dormir.', 'Pequeño Lector', 'G. Narrador', TRUE, 14.30, 2016, 20, NULL, 4);

-- Rol por defecto para clientes
INSERT INTO GRUPO (nombre) VALUES ('clientes');
