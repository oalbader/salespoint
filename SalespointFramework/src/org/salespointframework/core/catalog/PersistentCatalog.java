package org.salespointframework.core.catalog;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.salespointframework.core.database.Database;
import org.salespointframework.core.product.PersistentProductType;
import org.salespointframework.core.product.PersistentProductType_;
import org.salespointframework.core.product.ProductTypeIdentifier;
import org.salespointframework.util.ArgumentNullException;
import org.salespointframework.util.Iterables;
import org.salespointframework.util.Objects;

/**
 * TODO
 * @author Paul Henke
 * 
 */

public class PersistentCatalog implements Catalog<PersistentProductType>
{
	private final EntityManagerFactory emf = Database.INSTANCE.getEntityManagerFactory();

	/**
	 * Creates a new PersistentCatalog.
	 */
	public PersistentCatalog()
	{

	}

	@Override
	public final void add(PersistentProductType productType)
	{
		Objects.requireNonNull(productType, "productType");
		EntityManager em = emf.createEntityManager();
		em.persist(productType);
		beginCommit(em);
	}

	
	/**
	 * Adds multiple {@link PersistentProductType}s to this PersistentCatalog
	 * @param productTypes an Iterable of {@link PersistentProductType}s or subtypes to be added
	 */
	public final void addAll(Iterable<? extends PersistentProductType> productTypes)
	{
		Objects.requireNonNull(productTypes, "productTypes");
		EntityManager em = emf.createEntityManager();
		for (PersistentProductType productType : productTypes)
		{
			em.persist(productType);
		}
		beginCommit(em);
	}


	
	@Override
	public final boolean remove(ProductTypeIdentifier productIdentifier)
	{
		// TODO catch exception
		Objects.requireNonNull(productIdentifier, "productIdentifier");
		EntityManager em = emf.createEntityManager();
		Object productType = em.find(PersistentProductType.class, productIdentifier);
		if(productType != null)
		{
			em.remove(productType);
			beginCommit(em);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public final boolean contains(ProductTypeIdentifier productIdentifier)
	{
		Objects.requireNonNull(productIdentifier, "productIdentifier");
		EntityManager em = emf.createEntityManager();
		return em.find(PersistentProductType.class, productIdentifier) != null;
	}

	@Override
	public final <T extends PersistentProductType> T get(Class<T> clazz, ProductTypeIdentifier productIdentifier)
	{
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(productIdentifier, "productIdentifier");
		EntityManager em = emf.createEntityManager();
		return em.find(clazz, productIdentifier);
	}

	@Override
	public final <T extends PersistentProductType> Iterable<T> find(Class<T> clazz)
	{
		Objects.requireNonNull(clazz, "clazz");

		EntityManager em = emf.createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		TypedQuery<T> tq = em.createQuery(cq);

		return Iterables.of(tq.getResultList());
	}

	@Override
	public final <T extends PersistentProductType> Iterable<T> findByName(Class<T> clazz, String name)
	{
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(name, "name");

		EntityManager em = emf.createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);
		cq.where(cb.like(entry.get(PersistentProductType_.name), name));
		TypedQuery<T> tq = em.createQuery(cq);

		return Iterables.of(tq.getResultList());
	}

	@Override
	public final <T extends PersistentProductType> Iterable<T> findByCategory(Class<T> clazz, String category)
	{
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(category, "category");

		EntityManager em = emf.createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> entry = cq.from(clazz);

		Predicate p2 = cb.isMember(category, entry.<Set<String>> get("categories"));

		// Overload Resolution fail?
		// Predicate p2 = cb.isMember(category,
		// entry.get(PersistentProductType_.categories));
		// PluralAttribute<PersistentProductType, Set<String>, String>
		// collection = PersistentProductType_.categories;
		// Expression<Set<String>> ex = entry.get(collection);

		cq.where(p2);
		TypedQuery<T> tq = em.createQuery(cq);

		return Iterables.of(tq.getResultList());
	}

	/**
	 * Updates and persists an existing {@link PersistentProductType} to the PersistentCatalog and the Database
	 * @param productType the {@link PersistentProductType} to be updated
	 * @throws ArgumentNullException if productType is null
	 */
	public final void update(PersistentProductType productType)
	{
		Objects.requireNonNull(productType, "productType");
		EntityManager em = emf.createEntityManager();
		em.merge(productType);
		beginCommit(em);
	}

	private final void beginCommit(EntityManager entityManager)
	{
		entityManager.getTransaction().begin();
		entityManager.getTransaction().commit();
	}
}
