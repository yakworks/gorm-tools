package grails.plugin.dao

import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.springframework.transaction.interceptor.TransactionAspectSupport
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

class GormDaoSupport {
	static def log = org.apache.log4j.Logger.getLogger(GormDaoSupport)
	//injected bean
	//def grailsApplication
	
	boolean flushOnSave = false
	
	boolean fireEvents = true

	private Class thisDomainClass
	
	
	
	public GormDaoSupport(){}
	
	public GormDaoSupport(Class clazz){
		thisDomainClass = clazz
	}
	
	public GormDaoSupport(Class clazz,boolean fireEvents){
		thisDomainClass = clazz
		this.fireEvents = fireEvents
	}
	
	/**
	 * returns an instance with fireEvents=false and flushOnSave=false
	 */
	static GormDaoSupport getInstance(Class clazz){
/*		def dao = DaoUtil.ctx.getBean("gormDaoBean")
		dao.domainClass = clazz
		return dao*/
		def dao = new GormDaoSupport(clazz,false)
		return dao
	}

	//override this to set the domain this dao is for
	Class getDomainClass(){ return thisDomainClass}
	//set this is constructing a base dao by hand
	void setDomainClass(Class clazz){ thisDomainClass = clazz}

	/**
	* saves a domain entity and rewraps ValidationException with DomainException on error
	*
	* @param  entity  the domain entity to call save on
	* @throws DomainException if a validation or DataAccessException error happens
	*/
	def save(entity) {
		save(entity,[flush:flushOnSave])		
	}
	
	/**
	* saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error
	*
	* @param  entity  the domain entity to call save on
	* @param  args  the arguments to pass to save
	* @throws DomainException if a validation or DataAccessException error happens
	*/
	def save(entity, Map args) {
		args['failOnError'] = true
		try{
			if(fireEvents) DaoUtil.triggerEvent(this,"beforeSave", entity,null)
			entity.save(args)
		}
		catch (ValidationException ve){
			if(ve instanceof DomainException) throw ve //if this is already fired 
			throw new DomainException(DaoMessage.notSaved(entity), entity, ve.errors, ve)
		}
		catch (DataAccessException dae) {
			log.error("unexpected dao save error on ${entity.id} of ${entity.class.name}",dae)
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
	* @param  entity  the domain entity
	* @throws DomainException if a spring DataIntegrityViolationException is thrown
	*/
	def delete(entity){
		try {
			if(fireEvents) DaoUtil.triggerEvent(this,"beforeDelete", entity,null)
			entity.delete(flush:true)
		}
		catch (DataIntegrityViolationException dae) {
			def ident = DaoMessage.badge(entity.id,entity)
			log.error("dao delete error on ${entity.id} of ${entity.class.name}",dae)
			throw new DomainException(DaoMessage.notDeleted(entity,ident), entity,dae)
		}
	}

	/**
	* inserts and calls save for a new domain entity based with the data from params
	*
	* @param  params  the parameter map
	* @throws DomainException if a validation error happens
	*/
	Map insert( params) {
		formatParams(params)
		def entity = domainClass.newInstance()
		entity.properties = params
		if(fireEvents) DaoUtil.triggerEvent(this,"beforeInsertSave", entity, params)
		save(entity)
		return [ok:true,entity: entity, message:DaoMessage.created(entity)]
	}


	/**
	* updates a new domain entity with the data from params
	*
	* @param  params  the parameter map
	* @throws DomainException if a validation error happens or its not found with the params.id or the version is off and someone else edited it
	*/
	Map update( params){
		def entity = domainClass.get(params.id.toLong())
		formatParams(params)

		DaoUtil.checkFound(entity,params,domainClass.name)
		DaoUtil.checkVersion(entity,params.version)

		entity.properties = params
		if(fireEvents) DaoUtil.triggerEvent(this,"beforeUpdateSave", entity,params)
		save(entity)
		return [ ok:true, entity: entity,message:DaoMessage.updated(entity)]

	}

	/**
	* deletes a new domain entity base on the id in the params
	*
	* @param  params  the parameter map that has the id for the domain entity to delete
	* @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
	*/
	Map remove( params){
		def entity = domainClass.get(params.id.toLong())
		DaoUtil.checkFound(entity,params,domainClass.name)
		if(fireEvents) DaoUtil.triggerEvent(this,"beforeRemoveSave", entity,params)
		def msg = DaoMessage.deleted(entity,DaoMessage.badge(entity.id,entity))
		delete(entity)
		return [ok:true, id: params.id,message:msg]
	}
	
	//some our standard naming conventions on fields to clean up
	void formatParams(params){
		params.each{
			if(it.key.toLowerCase().endsWith("amount") || it.key.toLowerCase().endsWith("amount2") || it.key.toLowerCase().endsWith("price")
					|| it.key.toLowerCase().startsWith("unitprice")){
				it.value = it.value.toString().replace('$', '').replace(',', '')
			}
			if(it.key.toLowerCase().endsWith("percent") || it.key.toLowerCase().endsWith("pct")){
				 it.value = it.value.toString().replace('%', '')
			}
			if(it.key.toLowerCase().equals("docdate") || it.key.toLowerCase().equals("discdate") || it.key.toLowerCase().equals("duedate") || it.key.toLowerCase().endsWith("duedate")) {
				if(it.value && it.value instanceof String){
					it.value = new Date(it.value)
				}
			}
		}
	}

}

