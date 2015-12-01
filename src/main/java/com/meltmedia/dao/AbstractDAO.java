package com.meltmedia.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.meltmedia.data.IEntity;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Bare implementation of a DAO for simple entities with OpenJPA.
 * 
 * @author Devon Tackett
 * 
 */
public abstract class AbstractDAO<E extends IEntity> implements IDAO<E> {

  @Inject Provider<EntityManager> provider;

  protected Class<E> entityClass;
  
  public AbstractDAO( Class<E> entityClass ) {
    this.entityClass = entityClass;
  }
    
  public E get(Long id) {
    return provider.get().find(entityClass, id);
  }
  
  public E create(E entity) {
    provider.get().persist(entity);
    provider.get().flush();
    return entity;
  }
  
  public E update(E entity) {
    provider.get().persist(entity);
    provider.get().flush();
    return entity;
  }
  
  public E refresh(E entity) {
    provider.get().refresh(entity);
    return entity;
  }
  
  public Boolean delete(E entity) {
    provider.get().remove(entity);
    provider.get().flush();
    
    return true;
  }
  
  public Boolean lock(E entity) {
    provider.get().lock(entity, LockModeType.WRITE);
    return true;
  }
  
  public Boolean deleteById( long id ) {
    E entity = get(id);
    if( entity == null ) return true;
    provider.get().remove(entity);
    
    return true;
  }
  
  public List<E> list() {
    CriteriaBuilder builder = provider.get().getCriteriaBuilder();
    
    CriteriaQuery<E> criteria = builder.createQuery( entityClass );

    Root<E> listRoot = criteria.from( entityClass );

    criteria.select( listRoot );
    
    List<E> entities = provider.get().createQuery(criteria).getResultList();
    
    return entities;
  }

  public List<E> list(int page, int limit) {
    CriteriaBuilder builder = provider.get().getCriteriaBuilder();
    
    CriteriaQuery<E> criteria = builder.createQuery( entityClass );

    Root<E> listRoot = criteria.from( entityClass );

    criteria.select( listRoot );
    criteria.orderBy(builder.asc(listRoot.get("createdDate")));
    
    List<E> fullUserList = provider.get().createQuery(criteria).getResultList();
    int firstResult = page * limit;
    
    if(firstResult >= fullUserList.size())
    {
      int lastPageNumber = (int) (fullUserList.size() / limit);
     
      firstResult = lastPageNumber * limit;
       System.out.println("Users:" + fullUserList.size() + " , limit:" + limit 
        + " , lastPageNumber:" + lastPageNumber + ", firstResult:" + firstResult);

      System.out.println("You tried a page with limits well past our user count, I fixed it for you");
    }

    //somehow if you ended up with a first result of 0, fix it
    firstResult = Math.min(firstResult,0);
    
    List<E> entities = provider.get().createQuery(criteria).setFirstResult(firstResult).setMaxResults(limit).getResultList();

    return entities;
  }  
}