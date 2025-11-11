package enterpriseBeans;

import entidades.Cliente;
import entidades.Libro;
import entidades.LibroVendido;
import entidades.Pedido;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

@Stateful
public class CarroCompraEJB implements Serializable {

    public static class LibroEnCarro implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Libro libro;
        private int cantidad;

        public LibroEnCarro(Libro libro) {
            this.libro = libro;
            this.cantidad = 1;
        }

        public Libro getLibro() {
            return libro;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = Math.max(0, cantidad);
        }

        public BigDecimal getSubtotal() {
            if (libro.getPrecio() == null) {
                return BigDecimal.ZERO;
            }
            return libro.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private final Map<Long, LibroEnCarro> lineas = new LinkedHashMap<>();

    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    public void addLibro(Libro libro) {
        if (libro == null || libro.getId() == null) {
            return;
        }
        Libro persisted = em.find(Libro.class, libro.getId());
        if (persisted == null || !persisted.isDisponible()) {
            return;
        }
        LibroEnCarro existente = lineas.get(persisted.getId());
        if (existente == null) {
            existente = new LibroEnCarro(persisted);
            lineas.put(persisted.getId(), existente);
        }
        if (hayInventarioDisponible(persisted, existente.getCantidad() + 1)) {
            existente.setCantidad(existente.getCantidad() + 1);
        }
    }

    public void actualizarCantidad(Long libroId, int cantidad) {
        LibroEnCarro linea = lineas.get(libroId);
        if (linea == null) {
            return;
        }
        if (cantidad <= 0) {
            lineas.remove(libroId);
            return;
        }
        Libro libro = em.find(Libro.class, libroId);
        int max = libro != null && libro.getInventario() != null ? libro.getInventario() : cantidad;
        linea.setCantidad(Math.min(cantidad, max));
    }

    public void eliminarLibro(Long libroId) {
        lineas.remove(libroId);
    }

    public List<LibroEnCarro> getLibros() {
        limpiarLineasInvalidas();
        return new ArrayList<>(lineas.values());
    }

    public boolean estaVacio() {
        limpiarLineasInvalidas();
        return lineas.isEmpty();
    }

    public BigDecimal getTotal() {
        return getLibros().stream()
                .map(LibroEnCarro::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void vaciar() {
        lineas.clear();
    }

    private void limpiarLineasInvalidas() {
        Iterator<Map.Entry<Long, LibroEnCarro>> iterator = lineas.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, LibroEnCarro> entry = iterator.next();
            if (entry.getValue().getCantidad() <= 0) {
                iterator.remove();
            }
        }
    }

    private boolean hayInventarioDisponible(Libro libro, int cantidad) {
        return libro.getInventario() != null && libro.getInventario() >= cantidad;
    }

    @RolesAllowed("clientes")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Pedido confirmarPedido(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalStateException("Debe haber un cliente autenticado");
        }
        if (estaVacio()) {
            throw new IllegalStateException("El carro está vacío");
        }
        Cliente managedCliente = em.find(Cliente.class, cliente.getId());
        if (managedCliente == null) {
            throw new IllegalStateException("Cliente no encontrado");
        }
        Pedido pedido = new Pedido();
        pedido.setEstado("pendiente");
        pedido.setFecha(LocalDateTime.now());
        pedido.setImporte(getTotal());

        for (LibroEnCarro linea : getLibros()) {
            Libro libro = em.find(Libro.class, linea.getLibro().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (libro == null) {
                continue;
            }
            int inventarioActual = libro.getInventario() != null ? libro.getInventario() : 0;
            if (inventarioActual < linea.getCantidad()) {
                throw new IllegalStateException("No hay stock suficiente para " + libro.getTitulo());
            }
            libro.setInventario(inventarioActual - linea.getCantidad());

            LibroVendido vendido = new LibroVendido();
            vendido.setLibro(libro);
            vendido.setCantidad(linea.getCantidad());
            vendido.setImporte(libro.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidad())));
            pedido.addLibroVendido(vendido);
        }

        managedCliente.addPedido(pedido);
        em.persist(pedido);
        vaciar();
        return pedido;
    }
}
