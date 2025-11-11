package entidades;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "GRUPO")
public class Grupo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 100)
    private String nombre;

    @ManyToMany(mappedBy = "grupos")
    private Set<Cliente> clientes = new HashSet<>();

    public Grupo() {
    }

    public Grupo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(Set<Cliente> clientes) {
        this.clientes = clientes;
    }
}
