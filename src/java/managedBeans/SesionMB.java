package managedBeans;

import enterpriseBeans.CarroCompraEJB;
import enterpriseBeans.CarroCompraEJB.LibroEnCarro;
import enterpriseBeans.CatalogoEJB;
import enterpriseBeans.ClienteEJB;
import entidades.Cliente;
import entidades.Libro;
import entidades.Pedido;
import entidades.Tema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Named("sesionMB")
@SessionScoped
public class SesionMB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter LOGIN_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
    private static final DateTimeFormatter PEDIDO_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @EJB
    private CatalogoEJB catalogoEJB;

    @EJB
    private CarroCompraEJB carroCompraEJB;

    @EJB
    private ClienteEJB clienteEJB;

    private Tema temaSeleccionado;
    private Libro libroSeleccionado;
    private Cliente clienteAutenticado;

    private String login;
    private String password;

    private String nombre;
    private String direccion;
    private String mail;
    private String passwordRegistro;
    private String passwordConfirmacion;

    private boolean confirmarPedidoPendiente;
    private LocalDateTime loginTimestamp;

    public List<Tema> getTemas() {
        return catalogoEJB.getTemas();
    }

    public String verTema(Tema tema) {
        this.temaSeleccionado = tema;
        return "listaLibros?faces-redirect=true";
    }

    public Tema getTemaSeleccionado() {
        return temaSeleccionado;
    }

    public List<Libro> getLibrosDelTema() {
        return catalogoEJB.getLibros(temaSeleccionado);
    }

    public String verLibro(Libro libro) {
        this.libroSeleccionado = catalogoEJB.getDetallesLibro(libro);
        return "detallesLibro?faces-redirect=true";
    }

    public Libro getLibroSeleccionado() {
        return libroSeleccionado;
    }

    public String agregarAlCarro(Libro libro) {
        carroCompraEJB.addLibro(libro);
        return "carroCompra?faces-redirect=true";
    }

    public List<LibroEnCarro> getLibrosEnCarro() {
        return carroCompraEJB.getLibros();
    }

    public BigDecimal getTotalCarro() {
        return carroCompraEJB.getTotal();
    }

    public String irAlCarro() {
        return "carroCompra?faces-redirect=true";
    }

    public String actualizarLinea(Long libroId, int cantidad) {
        carroCompraEJB.actualizarCantidad(libroId, cantidad);
        return null;
    }

    public String eliminarDelCarro(Long libroId) {
        carroCompraEJB.eliminarLibro(libroId);
        return null;
    }

    public boolean isCarroVacio() {
        return carroCompraEJB.estaVacio();
    }

    public String irAConfirmacionPedido() {
        if (isCarroVacio()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "El carro está vacío", null));
            return null;
        }
        if (!isAutenticado()) {
            confirmarPedidoPendiente = true;
            return "login?faces-redirect=true";
        }
        return "pedido?faces-redirect=true";
    }

    public String confirmarPedido() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!isAutenticado()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Debe iniciar sesión antes de confirmar el pedido", null));
            return "login?faces-redirect=true";
        }
        try {
            Pedido pedido = carroCompraEJB.confirmarPedido(clienteAutenticado);
            clienteAutenticado = clienteEJB.findCliente(clienteAutenticado.getLogin()).orElse(clienteAutenticado);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Pedido " + pedido.getId() + " registrado", null));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "listaPedidos?faces-redirect=true";
        } catch (Exception ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        }
    }

    public String cancelarConfirmacion() {
        confirmarPedidoPendiente = false;
        return "carroCompra?faces-redirect=true";
    }

    public List<Pedido> getPedidosCliente() {
        return clienteEJB.obtenerPedidosCliente(clienteAutenticado);
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) external.getRequest();
        try {
            clienteAutenticado = clienteEJB.login(login, password, request);
            external.getFlash().setKeepMessages(true);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Bienvenido " + clienteAutenticado.getNombre(), null));
            limpiarCredenciales();
            loginTimestamp = LocalDateTime.now();
            if (confirmarPedidoPendiente) {
                confirmarPedidoPendiente = false;
                return "pedido?faces-redirect=true";
            }
            return "inicio?faces-redirect=true";
        } catch (Exception ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        }
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            clienteEJB.logout(request);
        } catch (ServletException ex) {
            // Ignorar
        }
        clienteAutenticado = null;
        confirmarPedidoPendiente = false;
        carroCompraEJB.vaciar();
        loginTimestamp = null;
        return "inicio?faces-redirect=true";
    }

    public String irARegistro() {
        return "registro?faces-redirect=true";
    }

    public String registrar() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (passwordRegistro == null || !passwordRegistro.equals(passwordConfirmacion)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Las contraseñas no coinciden", null));
            return null;
        }
        try {
            Cliente nuevo = clienteEJB.registrar(nombre, direccion, mail, login, passwordRegistro);
            limpiarFormularioRegistro();
            clienteAutenticado = clienteEJB.login(nuevo.getLogin(), passwordRegistro,
                    (HttpServletRequest) context.getExternalContext().getRequest());
            loginTimestamp = LocalDateTime.now();
            if (confirmarPedidoPendiente) {
                confirmarPedidoPendiente = false;
                return "pedido?faces-redirect=true";
            }
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registro completado", null));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "inicio?faces-redirect=true";
        } catch (Exception ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "registroError?faces-redirect=true";
        }
    }

    public boolean isAutenticado() {
        return clienteAutenticado != null;
    }

    public Cliente getClienteAutenticado() {
        return clienteAutenticado;
    }

    public String getNombreCliente() {
        return isAutenticado() ? clienteAutenticado.getNombre() : null;
    }

    public String getLoginTimestamp() {
        if (!isAutenticado()) {
            return null;
        }
        return LOGIN_FORMATTER.format(loginTimestamp != null ? loginTimestamp : LocalDateTime.now());
    }

    public String formatearFecha(LocalDateTime fecha) {
        return fecha == null ? "" : PEDIDO_FORMATTER.format(fecha);
    }

    private void limpiarCredenciales() {
        login = null;
        password = null;
    }

    private void limpiarFormularioRegistro() {
        nombre = null;
        direccion = null;
        mail = null;
        login = null;
        passwordRegistro = null;
        passwordConfirmacion = null;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPasswordRegistro() {
        return passwordRegistro;
    }

    public void setPasswordRegistro(String passwordRegistro) {
        this.passwordRegistro = passwordRegistro;
    }

    public String getPasswordConfirmacion() {
        return passwordConfirmacion;
    }

    public void setPasswordConfirmacion(String passwordConfirmacion) {
        this.passwordConfirmacion = passwordConfirmacion;
    }
}
