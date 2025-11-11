package managedBeans;

import enterpriseBeans.CarroCompraEJB;
import enterpriseBeans.CatalogoEJB;
import enterpriseBeans.ClienteEJB;
import entidades.Cliente;
import entidades.Libro;
import entidades.Tema;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author jrvidal
 */
@Named(value = "sesionMB")
@SessionScoped
public class SesionMB implements Serializable {

    @EJB
    private ClienteEJB clienteEJB;
    private Cliente cliente;
    private String errorMessage;
    private String nombre;
    private String mail;
    private String direccion;
    private String login;
    private String password;
    private String password2;
    @EJB
    private CatalogoEJB catalogoEJB;
    private Tema tema;
    private Libro libro;
    @EJB
    private CarroCompraEJB carroCompraEJB;
    private String loginTime;
    String paginaAnterior = "inicio";

    public String paginaAnterior() {
        return paginaAnterior;
    }

    public String login() {
        cliente = clienteEJB.login(login, password);
        if (isLogged()) {
            GregorianCalendar calendar = new GregorianCalendar();
            loginTime = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + ",  "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            return paginaAnterior;
        } else {
            return "";
        }
    }

    public String logout() {
        clienteEJB.logout();
        cliente = null;
        carroCompraEJB.vaciaCarro();       
        return paginaAnterior = "inicio";
    }

    public String registra() {
        errorMessage = clienteEJB.registra(nombre, direccion, mail, login, password, password2);
        if (errorMessage.equals("noError")) {
            return login();
        } else {
            return ("registroError");
        }
    }

    public String ponEnCarro() {
        carroCompraEJB.ponEnCarro(libro);
        return paginaAnterior = "carroCompra";
    }

    public String vaciaCarro() {
        carroCompraEJB.vaciaCarro();
        return "listaTemas";
    }

    public String confirmaPedido() {
        carroCompraEJB.confirmaPedido(cliente);
        return paginaAnterior = "listaPedidos";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<Tema> getTemas() {
        return catalogoEJB.todosLosTemas();
    }

    public String verTema(Tema tema) {
        this.tema = tema;
        return paginaAnterior = "listaLibros";
    }

    public String verLibro(Libro libro) {
        this.libro = libro;
        return paginaAnterior = "detallesLibro";
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public boolean isLogged() {
        return cliente != null;
    }

    public String getNombre() {
        if (isLogged()) {
            return cliente.getNombre();
        } else {
            return "";
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLogin() {
        if (isLogged()) {
            return cliente.getLogin();
        } else {
            return "";
        }
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return "";
    }

    public String getPassword2() {
        return "";
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getMail() {
        if (isLogged()) {
            return cliente.getMail();
        } else {
            return "";
        }
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDireccion() {
        if (isLogged()) {
            return cliente.getDireccion();
        } else {
            return "";
        }
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Tema getTema() {
        return tema;
    }

    public Libro getLibro() {
        return libro;
    }

    public CarroCompraEJB getCarroCompra() {
        return carroCompraEJB;
    }
}
