package com.tienda.web;

import com.tienda.ejb.CarroCompraEJB;
import com.tienda.ejb.CatalogoEJB;
import com.tienda.ejb.ClienteEJB;
import com.tienda.ejb.CarroCompraEJB.LibroEnCarro;
import com.tienda.model.Cliente;
import com.tienda.model.Libro;
import com.tienda.model.Pedido;
import com.tienda.model.Tema;
import java.io.Serializable;
import java.math.BigDecimal;
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

    public List<Tema> getTemas() {
        return catalogoEJB.getTemas();
    }

    public String verTema(Tema tema) {
        this.temaSeleccionado = tema;
        return "/views/listaLibros?faces-redirect=true";
    }

    public Tema getTemaSeleccionado() {
        return temaSeleccionado;
    }

    public List<Libro> getLibrosDelTema() {
        return catalogoEJB.getLibros(temaSeleccionado);
    }

    public String verLibro(Libro libro) {
        this.libroSeleccionado = catalogoEJB.getDetallesLibro(libro);
        return "/views/detallesLibro?faces-redirect=true";
    }

    public Libro getLibroSeleccionado() {
        return libroSeleccionado;
    }

    public String agregarAlCarro(Libro libro) {
        carroCompraEJB.addLibro(libro);
        return "/views/carroCompra?faces-redirect=true";
    }

    public List<LibroEnCarro> getLibrosEnCarro() {
        return carroCompraEJB.getLibros();
    }

    public BigDecimal getTotalCarro() {
        return carroCompraEJB.getTotal();
    }

    public String irAlCarro() {
        return "/views/carroCompra?faces-redirect=true";
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
            return "/views/login?faces-redirect=true";
        }
        return "/views/pedido?faces-redirect=true";
    }

    public String confirmarPedido() {
        if (!isAutenticado()) {
            confirmarPedidoPendiente = true;
            return "/views/login?faces-redirect=true";
        }
        try {
            carroCompraEJB.confirmarPedido(clienteAutenticado);
            actualizarPedidosCliente();
            return "/views/listaPedidos?faces-redirect=true";
        } catch (IllegalStateException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        }
    }

    public String cancelarConfirmacion() {
        confirmarPedidoPendiente = false;
        return "/views/carroCompra?faces-redirect=true";
    }

    public List<Pedido> getPedidosCliente() {
        return clienteAutenticado == null ? java.util.Collections.emptyList() : clienteEJB.obtenerPedidosCliente(clienteAutenticado);
    }

    private void actualizarPedidosCliente() {
        if (clienteAutenticado != null) {
            clienteAutenticado.setPedidos(clienteEJB.obtenerPedidosCliente(clienteAutenticado));
        }
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            clienteAutenticado = clienteEJB.login(login, password, request);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Bienvenido " + clienteAutenticado.getNombre(), null));
            limpiarCredenciales();
            if (confirmarPedidoPendiente) {
                confirmarPedidoPendiente = false;
                return "/views/pedido?faces-redirect=true";
            }
            return "/views/inicio?faces-redirect=true";
        } catch (ServletException | IllegalArgumentException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Login inválido", ex.getMessage()));
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
        return "/views/inicio?faces-redirect=true";
    }

    public String irARegistro() {
        return "/views/registro?faces-redirect=true";
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
            if (confirmarPedidoPendiente) {
                confirmarPedidoPendiente = false;
                return "/views/pedido?faces-redirect=true";
            }
            return "/views/inicio?faces-redirect=true";
        } catch (Exception ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "/views/registroError?faces-redirect=true";
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
