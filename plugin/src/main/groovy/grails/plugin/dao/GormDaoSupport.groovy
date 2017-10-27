package grails.plugin.dao

import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

//import grails.gorm.transactions.Transactional
import grails.transaction.Transactional
import grails.validation.ValidationException
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

import org.springframework.core.GenericTypeResolver
import gorm.tools.hibernate.criteria.CriteriaUtils

@SuppressWarnings(['EmptyMethod'])
@GrailsCompileStatic
@Transactional
class GormDaoSupport<T extends GormEntity & WebDataBinding> {

	boolean flushOnSave = false
	boolean fireEvents = true

	private Class<T> thisDomainClass

	GormDaoSupport() {
		this.thisDomainClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), GormDaoSupport.class)
	}

	GormDaoSupport(Class<T> clazz) {
		thisDomainClass = clazz
	}

	GormDaoSupport(Class<T> clazz, boolean fireEvents) {
		thisDomainClass = clazz
		this.fireEvents = fireEvents
	}

	/**
	 * returns an instance with fireEvents=false and flushOnSave=false
	 */
	//TODO: investigate why it doesnt work without it
	@CompileDynamic
	static GormDaoSupport<T> getInstance(Class<T> clazz) {
		GormDaoSupport dao = grails.plugin.dao.DaoUtil.ctx.getBean("gormDaoBean")
		dao.domainClass = clazz
		return dao
		return new GormDaoSupport(clazz, false)

	}

	//override this to set the domain this dao is for
	Class<T> getDomainClass() { return thisDomainClass }
	//set this is constructing a base dao by hand
	void setDomainClass(Class<T> clazz) { thisDomainClass = clazz }

	/**
	 * saves a domain entity and rewraps ValidationException with DomainException on error
	 *
	 * @param entity the domain entity to call save on
	 * @throws DomainException if a validation or DataAccessException error happens
	 */
	T save(T entity) {
		save(entity, [flush: flushOnSave])
	}

	/**
	 * saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error
	 *
	 * @param entity the domain entity to call save on
	 * @param args the arguments to pass to save
	 * @throws DomainException if a validation or DataAccessException error happens
	 */
	T save(T entity, Map args) {
		return doSave(entity, args)
	}

	protected final T doSave(T entity, Map args) {
		args['failOnError'] = true
		try {
			if (fireEvents) beforeSave(entity)
			return entity.save(args)
		}
		catch (ValidationException ve) {
			if (ve instanceof DomainException) throw ve //if this is already fired
			throw new DomainException(DaoMessage.notSaved(entity), entity, ve.errors, ve)
		}
		catch (DataAccessException dae) {
			//log.error("unexpected dao save error on ${entity.id} of ${entity.class.name}",dae)
			//TODO we can build a better message with optimisticLockingFailure(entity) if dae.cause instanceof org.springframework.dao.OptimisticLockingFailureException
			//TODO also, in the case of optimisticLocking, is that really un expected? shoud we log it?
			//TODO we shold really chnage the message from the default notSaved as this is more of a critical low level error a
			//and save the default notSaved for when a validation occurs like above
			throw new DomainException(DaoMessage.notSaved(entity), entity, dae) //make a DaoMessage.notSavedDataAccess
		}
	}

	/**
	 * calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions
	 *
	 * @param entity the domain entity
	 * @throws DomainException if a spring DataIntegrityViolationException is thrown
	 */
	void delete(T entity) {
		doDelete(entity)
	}

	protected final void doDelete(T entity) {
		try {
			if (fireEvents) beforeDelete(entity)
			entity.delete(flush: true)
		}
		catch (DataIntegrityViolationException dae) {
			String ident = DaoMessage.badge(entity.ident(), entity)
			//log.error("dao delete error on ${entity.id} of ${entity.class.name}",dae)
			throw new DomainException(DaoMessage.notDeleted(entity, ident), entity, dae)
		}
	}

	/**
	 * inserts and calls save for a new domain entity based with the data from params
	 *
	 * @param params the parameter map
	 * @throws DomainException if a validation error happens
	 */
	Map<String, Object> insert(Map params) {
		return doInsert(params)
	}

	//See at the bottom why we need this doXX methods
	protected final Map<String, Object> doInsert(Map params) {
		T entity = domainClass.newInstance()
		entity.properties = params
		if (fireEvents) beforeInsertSave(entity, params)
		save(entity)
		return [ok: true, entity: entity, message: DaoMessage.created(entity)]
	}

	/**
	 * updates a new domain entity with the data from params
	 *
	 * @param params the parameter map
	 * @throws DomainException if a validation error happens or its not found with the params.id or the version is off and someone else edited it
	 */
	Map<String, Object> update(Map params) {
		return doUpdate(params)
	}

	/**
	 *
	 *
	 * @param params
	 * @param closure
	 * @return
	 */
	@CompileDynamic
	List<T> search(Map params = [:], Closure closure = null) {
		Map criteria
		if (params['criteria'] instanceof String) { //TODO: keyWord `criteria` probably should be driven from config
			criteria = JSON.parse(params['criteria']) as Map
		} else {
			criteria = params['criteria'] as Map ?: [:]
		}
		CriteriaUtils.list(criteria, this.thisDomainClass, params as Map, closure)
	}

	protected final Map<String, Object> doUpdate(Map params) {
		T entity = get(params.id as Serializable)

		DaoUtil.checkFound(entity, params, domainClass.name)
		DaoUtil.checkVersion(entity, params.version)

		entity.properties = params
		if (fireEvents) beforeUpdateSave(entity, params)
		save(entity)
		return [ok: true, entity: entity, message: DaoMessage.updated(entity)]
	}

	/**
	 * deletes a new domain entity base on the id in the params
	 *
	 * @param params the parameter map that has the id for the domain entity to delete
	 * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
	 */
	Map remove(Map params) {
		return doRemove(params)
	}

	protected final Map doRemove(Map params) {
		T entity = get(params.id as Serializable)
		DaoUtil.checkFound(entity, params, domainClass.name)
		if (fireEvents) beforeRemoveSave(entity, params)
		Map msg = DaoMessage.deleted(entity, DaoMessage.badge(entity.ident(), entity))
		delete(entity)
		return [ok: true, id: params.id, message: msg]
	}

	@CompileDynamic
	protected T get(Serializable id) {
		return domainClass.get(id)
	}

	//event templates
	protected void beforeSave(T entity) { }

	protected void beforeDelete(T entity) { }

	protected void beforeInsertSave(T entity, Map params) { }

	protected void beforeUpdateSave(T entity, Map params) { }

	protected void beforeRemoveSave(T entity, Map params) { }

}

/*
 We had to add doInsert, doSave, doUpdate etc methods to GormDaoSupport to circumvent this issue: https://github.com/grails/grails-core/issues/10681
 When extending a Generic base class which is Transactional. If child classes overrides any of the base class methods, and tries to call the super class method from it.
 eg super.save() it results in StackOverflowError.

 Many of our daos override the methods from GormDaoSupport and from the overriden method it calls the superclass version of the method, resulting in StackOverflow.
 the doXX methods are provided, so that, child classes can call the super.doX version of the method from overriden method rather then calling super.insert.
 */