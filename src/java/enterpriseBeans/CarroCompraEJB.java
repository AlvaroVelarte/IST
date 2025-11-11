package enterpriseBeans;

import entidades.Cliente;
import entidades.Libro;
import entidades.LibroVendido;
import entidades.Pedido;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author jrvidal
 */
@Stateful
public class CarroCompraEJB {

    public class LibroEnCarro {

        final private Libro libro;
        private int cantidad;

        public LibroEnCarro(Libro libro) {
            this.libro = libro;
            cantidad = 1;
        }

        public Libro getLibro() {
            return libro;
        }

        public int getCantidad() {
            cantidad = Math.min(libro.getInventario(), cantidad);
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public float getSubTotal() {
            return cantidad * libro.getPrecio();
        }
    }

    final private List<LibroEnCarro> librosEnCarro = new ArrayList<>();
    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    public void ponEnCarro(Libro libro) {
        for (LibroEnCarro libroEnCarro : librosEnCarro) {
            if (libroEnCarro.getLibro().equals(libro)) {
                if (libroEnCarro.getCantidad() < libro.getInventario()) {
                    libroEnCarro.setCantidad(libroEnCarro.getCantidad() + 1);
                }
                return;
            }
        }
        librosEnCarro.add(new LibroEnCarro(libro));
    }

    public boolean isVacio() {
        return librosEnCarro.isEmpty();
    }

    public void vaciaCarro() {
        librosEnCarro.clear();
    }

    public double getTotal() {
        double total = 0;
        for (LibroEnCarro libroEnCarro : librosEnCarro) {
            total += libroEnCarro.getSubTotal();
        }
        return (total);
    }

    public List<LibroEnCarro> getLibrosEnCarro() {
        for (LibroEnCarro libroEnCarro : librosEnCarro) {
            if (libroEnCarro.getCantidad() == 0) {
                librosEnCarro.remove(libroEnCarro);
            }
        }
        return librosEnCarro;
    }

    public int getNumLibrosEnCarro() {
        return getLibrosEnCarro().size();
    }

    @RolesAllowed("clientes")
    public void confirmaPedido(Cliente cliente) {
        Pedido pedido = new Pedido();
        for (LibroEnCarro libroEnCarro : librosEnCarro) {
            libroEnCarro.getLibro().setInventario(libroEnCarro.getLibro().getInventario() - libroEnCarro.getCantidad());
            LibroVendido libroVendido = new LibroVendido();
            libroVendido.setLibro(libroEnCarro.getLibro());
            libroVendido.setCantidad(libroEnCarro.getCantidad());
            libroVendido.setImporte(libroEnCarro.getSubTotal());
            libroVendido.setPedido(pedido);
            pedido.getLibros().add(libroVendido);
        }
        pedido.setImporte(getTotal());
        pedido.setEstado("pendiente");
        GregorianCalendar calendar = new GregorianCalendar();
        pedido.setFecha(calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + ",  "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        pedido.setCliente(cliente);
        cliente.getPedidos().add(pedido);
        em.merge(cliente);
        vaciaCarro();
    }
}
