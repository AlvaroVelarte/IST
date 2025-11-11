package enterpriseBeans;

import entidades.Tema;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author jrvidal
 */
@Singleton
public class CatalogoEJB {

    @PersistenceContext(unitName = "tiendaPU")
    private EntityManager em;

    List<Tema> listaTemas;

    @PostConstruct
    public void init() {
        listaTemas = em.createQuery("SELECT t FROM Tema t", Tema.class).getResultList();
    }

    public List<Tema> todosLosTemas() {
        return listaTemas;
    }
}
