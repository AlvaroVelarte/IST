package com.tienda.ejb;

import com.tienda.model.Cliente;
import com.tienda.model.Grupo;
import com.tienda.model.Pedido;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Stateless
public class ClienteEJB {

    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    public Optional<Cliente> findCliente(String login) {
        if (login == null) {
            return Optional.empty();
        }
        TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c LEFT JOIN FETCH c.grupos WHERE c.login = :login",
                Cliente.class);
        query.setParameter("login", login);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public Cliente login(String login, String password, HttpServletRequest request) throws ServletException {
        if (request == null) {
            throw new IllegalArgumentException("La petición HTTP es obligatoria");
        }
        request.login(login, password);
        return findCliente(login)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales no válidas"));
    }

    public void logout(HttpServletRequest request) throws ServletException {
        if (request != null) {
            request.logout();
        }
    }

    public Cliente registrar(String nombre, String direccion, String mail, String login, String password) {
        validarRegistro(nombre, direccion, mail, login, password);
        if (findCliente(login).isPresent()) {
            throw new IllegalArgumentException("El login " + login + " ya está registrado");
        }
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setDireccion(direccion);
        cliente.setMail(mail);
        cliente.setLogin(login);
        cliente.setPwd(hashPassword(password));

        Grupo grupoClientes = em.find(Grupo.class, "clientes");
        if (grupoClientes == null) {
            grupoClientes = new Grupo("clientes");
            em.persist(grupoClientes);
        }
        cliente.getGrupos().add(grupoClientes);
        grupoClientes.getClientes().add(cliente);

        em.persist(cliente);
        return cliente;
    }

    public List<Pedido> obtenerPedidosCliente(Cliente cliente) {
        if (cliente == null) {
            return Collections.emptyList();
        }
        Cliente managed = em.find(Cliente.class, cliente.getId());
        if (managed == null) {
            return Collections.emptyList();
        }
        return em.createQuery("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.libros WHERE p.cliente = :cliente ORDER BY p.fecha DESC",
                Pedido.class)
                .setParameter("cliente", managed)
                .getResultList();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            byte[] hashed = digest.digest(password.getBytes("UTF-8"));
            return String.format("%0128x", new BigInteger(1, hashed));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        }
    }

    private void validarRegistro(String nombre, String direccion, String mail, String login, String password) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (mail == null || mail.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("El login es obligatorio");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }
}
