# Tienda de Libros (JSF + EJB + JPA)

Arquitectura de referencia para una tienda de libros online construida con Jakarta EE (JSF 2, EJB 3, JPA 2). Implementa los casos de uso básicos del cliente: consulta de catálogo, gestión del carro, confirmación de pedidos, consulta de pedidos, login y registro.

## Requisitos

* Java 8 o superior
* Servidor de aplicaciones compatible con Jakarta EE 8 (GlassFish 5 / Payara 5 / WildFly con perfil completo)
* Base de datos relacional accesible mediante `tiendaDS`
* Maven 3 para compilar

## Estructura

```
src/main/java
  com.tienda.model        -> Entidades JPA
  com.tienda.ejb          -> EJBs de catálogo, carro y cliente
  com.tienda.web          -> Managed Bean de sesión (JSF)

src/main/webapp
  index.xhtml             -> Redirección a vistas
  /views                  -> Páginas JSF
  /WEB-INF                -> Descriptores (web.xml, glassfish-web.xml, beans.xml)

src/main/resources/META-INF/persistence.xml -> Unidad de persistencia tiendaPU

db/tienda-schema.sql     -> DDL y datos iniciales de catálogo
```

## Construcción

```bash
mvn clean package
```

El resultado es un WAR (`target/tienda.war`).

## Despliegue y configuración

1. Crear la base de datos y ejecutar `db/tienda-schema.sql` para generar tablas, rol `clientes` y datos iniciales de catálogo.
2. Configurar el data source `tiendaDS` en el servidor apuntando a la base de datos anterior.
3. Desplegar el WAR en el servidor (contexto `/tienda`).
4. Verificar que la unidad de persistencia `tiendaPU` apunta a `java:app/tiendaDS` (configurado en `persistence.xml`).
5. Configurar un `JDBCRealm` en el servidor con:
   * Clase: `com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm`
   * JAAS Context: `jdbcRealm`
   * JNDI: `java:app/tiendaDS`
   * Tabla usuarios: `cliente`
   * Columna usuario: `login`
   * Columna password: `pwd` (hash SHA-512 en hexadecimal)
   * Tabla grupos: `cliente_grupo`
   * Columna usuario: `cliente_login`
   * Columna grupo: `grupos_nombre`
   * Algoritmo de resumen: `SHA-512`
6. Asegurar que el realm anterior es el predeterminado del dominio.
7. Reiniciar el servidor para aplicar la configuración.
8. Acceder a `http://<host>:<puerto>/tienda/views/inicio.xhtml` y comprobar el flujo completo:
   * Navegar por catálogo (temas → libros → detalles)
   * Añadir libros al carro y editar cantidades
   * Confirmar pedido (si no hay sesión, solicitará login)
   * Registrar nuevo cliente (se añade automáticamente al grupo `clientes`)
   * Consultar los pedidos del cliente autenticado

## Notas de implementación

* `CarroCompraEJB` es un EJB con estado que mantiene las líneas del carro y crea `Pedido`/`LibroVendido` en `confirmarPedido`, vaciando el carro y actualizando inventario.
* `SesionMB` mantiene el cliente autenticado, delega en los EJB y gestiona la navegación JSF.
* Las páginas JSF están preparadas para enlazar con el managed bean y navegar entre vistas.
* `web.xml` y `glassfish-web.xml` definen el rol `clientes` y su mapeo al grupo de seguridad homónimo.

Para contraseñas de usuarios insertadas manualmente, generar el hash SHA-512 en hexadecimal antes de persistirlo (por ejemplo usando `ClienteEJB#hashPassword`).
