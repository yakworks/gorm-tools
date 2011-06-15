package grails.plugin.dao

import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.springframework.transaction.interceptor.TransactionAspectSupport
import grails.validation.ValidationException
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

class GormDao {
	//injected bean
	def grailsApplication
	
	def flushOnSave = false

	private Class thisDomainClass
	
	public GormDao(){}
	
	public GormDao(Class clazz){
		thisDomainClass = clazz
	}
	
	static GormDao getBean(Class clazz){
		def dao = AH.application.mainContext.getBean("gormDaoBean")
		dao.domainClass = clazz
		return dao
	}

	//override this to set the domain this dao is for
	def getDomainClass(){ return thisDomainClass}
	//set this is constructing a base dao by hand
	def setDomainClass(clazz){ thisDomainClass = clazz}

	/**
	* saves a domain entity and rewraps ValidationException with GormDataException on error
	*
	* @param  params  the parameter map
	* @throws GormDataException if a validation error happens
	*/
	
	def save(entity) {
		try{
			DaoUtils.triggerEvent(this,"beforeSave", entity,null)
			entity.save(flush:flushOnSave,failOnError:true)
		}
		catch (ValidationException e){
			throw new GormDataException(DaoUtils.saveFailedMessage(entity), entity, e.errors)
		}
		catch (DataAccessException dae) {
			def ge = new GormDataException(DaoUtils.saveFailedMessage(entity), entity)
			ge.dataAccessException = dae
			throw ge
		}
	}

	/**
	* calls delete on the entity to remove it from the db
	*
	* @param  entity  the domain entity
	* @throws GormDataException if a spring DataIntegrityViolationException is thrown
	*/
	def delete(entity){
		try {
			DaoUtils.triggerEvent(this,"beforeDelete", entity,null)
			entity.delete(flush:flushOnSave)
		}
		catch (DataIntegrityViolationException e) {
			def ident = DaoUtils.badge(entity.id,entity)
			def ge = new GormDataException(DaoUtils.deleteMessage(entity,ident,false), entity)
			ge.dataAccessException = e
			throw ge
		}
	}

	/**
	* inserts and calls save for a new domain entity based with the data from params
	*
	* @param  params  the parameter map
	* @throws GormDataException if a validation error happens
	*/
	def insert(params) {
		formatParams(params)
		def entity = domainClass.newInstance()
		entity.properties = params
		DaoUtils.triggerEvent(this,"beforeInsertSave", entity, params)
		save(entity)
		return [ok:true,entity: entity, message:DaoUtils.createMessage(entity)]
	}


	/**
	* updates a new domain entity with the data from params
	*
	* @param  params  the parameter map
	* @throws GormDataException if a validation error happens or its not found with the params.id or the version is off and someone else edited it
	*/
	def update(params){
		def entity = domainClass.get(params.id.toLong())
		formatParams(params)

		DaoUtils.checkFound(entity,params,domainClass.name)
		DaoUtils.checkVersion(entity,params.version)

		entity.properties = params
		DaoUtils.triggerEvent(this,"beforeUpdateSave", entity,params)
		save(entity)
		return [ ok:true, entity: entity,message:DaoUtils.updateMessage(entity)]

	}

	/**
	* deletes a new domain entity base on the id in the params
	*
	* @param  params  the parameter map that has the id for the domain entity to delete
	* @throws GormDataException if its not found or if a DataIntegrityViolationException is thrown
	*/
	def remove(params){
		def entity = domainClass.get(params.id.toLong())
		DaoUtils.checkFound(entity,params,domainClass.name)
		DaoUtils.triggerEvent(this,"beforeRemoveSave", entity,params)
		def msg = DaoUtils.deleteMessage(entity,DaoUtils.badge(entity.id,entity),true)
		delete(entity)
		return [ok:true, id: params.id,message:msg]
	}
	
	void formatParams(params){
		params.each{
			if(it.key.toLowerCase().endsWith("amount") || it.key.toLowerCase().endsWith("amount2") || it.key.toLowerCase().endsWith("price")
					|| it.key.toLowerCase().startsWith("unitprice")){
				it.value = it.value.toString().replace('$', '').replace(',', '')
			}
			if(it.key.toLowerCase().endsWith("percent")){
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

