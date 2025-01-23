package services;

import connexion.EMF;
import connexion.EntityFinder;
import connexion.EntityFinderImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.reflect.ParameterizedType;
import java.util.List;


public abstract class Service<E> implements IService<E> {

    protected EntityManager em;
    protected EntityFinder<E> finder;

    private Class<?> ec;

    Service() {
        this.em = EMF.getEM();
        this.ec = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.finder = new EntityFinderImpl<>(ec, this.em);
    }

    public E getById (int id) {
        return finder.findOne(id);
    }

    public abstract E save(E e);

    public void delete(int id) {
        E e = (E) em.find(ec, id);
        em.remove(e);
    }

    public EntityTransaction getTransaction() {
        return em.getTransaction();
    }

    public void close() {
        em.close();
    }

    public void refreshCollection(List<E> entityCollection) {
        for (E entity : entityCollection) {
            refreshEntity(entity);
        }
    }

    public void refreshEntity(E entity) {
        if ( ! em.contains(entity))
            entity = em.merge(entity);

        em.refresh(entity);
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}
