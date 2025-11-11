package com.tienda.ejb;

import com.tienda.model.Libro;
import com.tienda.model.Tema;
import java.util.Collections;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CatalogoEJB {

    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    public List<Tema> getTemas() {
        return em.createQuery("SELECT t FROM Tema t ORDER BY t.nombre", Tema.class)
                .getResultList();
    }

    public List<Libro> getLibros(Tema tema) {
        if (tema == null) {
            return Collections.emptyList();
        }
        Tema persisted = em.find(Tema.class, tema.getId());
        if (persisted == null) {
            return Collections.emptyList();
        }
        return em.createQuery("SELECT l FROM Libro l WHERE l.tema = :tema ORDER BY l.titulo", Libro.class)
                .setParameter("tema", persisted)
                .getResultList();
    }

    public Libro getDetallesLibro(Libro libro) {
        if (libro == null) {
            return null;
        }
        return em.find(Libro.class, libro.getId());
    }

    public Libro findLibroById(Long id) {
        return id == null ? null : em.find(Libro.class, id);
    }
}
