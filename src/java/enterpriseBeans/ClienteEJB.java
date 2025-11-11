package enterpriseBeans;

import entidades.Cliente;
import entidades.Grupo;
import java.math.BigInteger;
import java.security.MessageDigest;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jrvidal
 */
@Stateless
public class ClienteEJB {

    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    public Cliente login(String login, String password) {
        try {
            Cliente cliente = em.find(Cliente.class, login);
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            request.login(login, password);
            return cliente;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void logout() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            request.logout();
        } catch (ServletException e) {
        }
    }

    public String registra(String nombre, String direccion, String mail, String login, String password, String password2) {
        if (nombre.isEmpty()) {
            return "El nombre no puede estar en blanco";
        } else if (direccion.isEmpty()) {
            return "La dirección no puede estar en blanco";
        } else if (mail.isEmpty()) {
            return "La dirección de correo no puede estar en blanco";
        } else if (login.isEmpty()) {
            return "El login no puede estar en blanco";
        } else if ((password.isEmpty()) || (!password.equals(password2))) {
            return "Las dos contraseñas introducidas no coinciden";
        } else {
            try {
                Cliente cliente = em.find(Cliente.class, login);                
                if (cliente == null) {
                    cliente = new Cliente();
                    cliente.setNombre(nombre);
                    cliente.setDireccion(direccion);
                    cliente.setLogin(login);
                    cliente.setMail(mail);
                    MessageDigest digest = MessageDigest.getInstance("SHA-512");
                    digest.reset();
                    digest.update(password.getBytes("utf8"));
                    cliente.setPwd(String.format("%0128x", new BigInteger(1, digest.digest())));
                    cliente.getGrupos().add(em.find(Grupo.class,"clientes"));
                    em.persist(cliente);
                    return "noError";
                } else {
                    return "El login " + login + " ya existe";
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }
}
