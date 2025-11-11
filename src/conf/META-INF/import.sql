INSERT INTO GRUPO (nombre) VALUES ('clientes');

INSERT INTO TEMA (id, nombre) VALUES (1, 'Ficción');
INSERT INTO TEMA (id, nombre) VALUES (2, 'Tecnología');
INSERT INTO TEMA (id, nombre) VALUES (3, 'Historia');

INSERT INTO LIBRO (id, titulo, isbn, descripcion, editorial, autor, disponible, precio, anio, inventario, foto, tema_id)
VALUES (1, 'El viaje de la IA', 'ISBN-001', 'Un viaje por la inteligencia artificial moderna.', 'TechBooks', 'A. Expert', TRUE, 29.90, 2022, 10, NULL, 2);
INSERT INTO LIBRO (id, titulo, isbn, descripcion, editorial, autor, disponible, precio, anio, inventario, foto, tema_id)
VALUES (2, 'Historia de Roma', 'ISBN-002', 'Desde el nacimiento de la República hasta el Imperio.', 'Historia Viva', 'M. Historian', TRUE, 24.50, 2018, 7, NULL, 3);
INSERT INTO LIBRO (id, titulo, isbn, descripcion, editorial, autor, disponible, precio, anio, inventario, foto, tema_id)
VALUES (3, 'Mundos Imaginarios', 'ISBN-003', 'Colección de relatos fantásticos.', 'Ficción Plus', 'L. Creator', TRUE, 19.99, 2020, 12, NULL, 1);
