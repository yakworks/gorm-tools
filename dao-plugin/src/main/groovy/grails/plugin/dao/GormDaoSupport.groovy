package grails.plugin.dao

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.transaction.Transactional
import grails.validation.ValidationException
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

@GrailsCompileStatic
@Transactional
class GormDaoSupport<T extends GormEntity & WebDataBinding> {

	boolean flushOnSave = false
	boolean fireEvents = true

	private Class<T> thisDomainClass

	GormDaoSupport() { }

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
	static GormDaoSupport<T> getInstance(Class<T> clazz) {
/*		def dao = DaoUtil.ctx.getBean("gormDaoBean")
		dao.domainClass = clazz
		return dao*/
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
			def ident = DaoMessage.badge(entity.ident(), entity)
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

	protected final Map<String, Object> doUpdate(Map params) {
		T entity = load(params.id as Long)

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
		T entity = load(params.id as Long)
		DaoUtil.checkFound(entity, params, domainClass.name)
		if (fireEvents) beforeRemoveSave(entity, params)
		Map msg = DaoMessage.deleted(entity, DaoMessage.badge(entity.ident(), entity))
		delete(entity)
		return [ok: true, id: params.id, message: msg]
	}

	@CompileDynamic
	private T load(Long id) {
		if (id == null) throw new NullPointerException("Id is null")
		return domainClass.get(id)
	}

	//event templates
	protected void beforeSave(T entity) { }

	protected void beforeDelete(T entity) { }

	protected void beforeInsertSave(T entity, Map params) { }

	protected void beforeUpdateSave(T entity, Map params) { }

	protected void beforeRemoveSave(T entity, Map params) { }

}

