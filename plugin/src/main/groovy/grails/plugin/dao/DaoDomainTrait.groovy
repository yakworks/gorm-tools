package grails.plugin.dao

import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.gorm.GormEntity
import gorm.tools.hibernate.criteria.CriteriaUtils
import grails.plugin.dao.*

@CompileStatic
trait DaoDomainTrait<D extends GormEntity> {
	static GormDaoSupport daoBean

	/**
	 * Looks up or creates and caches a dao bean
	 * @return The dao
	 *//*
	static GormDaoSupport getDao() {
		if(!daoBean) daoBean = DaoUtil.getDao(this)
		return daoBean
	}*/


	//TODO: investigate why it doesnt work without it
	/**
	 * Looks up or creates and caches a dao bean
	 * @return The dao
	 */
	static GormDaoSupport<D> getDao() {
		GrailsApplication grailsApplication = Holders.grailsApplication
		String domainName = this.name
		Class domainClass = grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domainName).clazz
		String daoName = "${GrailsNameUtils.getPropertyName(domainName)}Dao"
		GormDaoSupport<D> dao
		if(grailsApplication.mainContext.containsBean(daoName)){
			dao = (GormDaoSupport<D>)grailsApplication.mainContext.getBean(daoName)
		}else{
			dao = (GormDaoSupport<D>)grailsApplication.mainContext.getBean("gormDaoBean")
			dao.domainClass = domainClass
		}
		if(!dao){
			dao = GormDaoSupport.getInstance(domainClass)
		}
		return dao
	}

	D persist(Map args) {
		args['failOnError'] = true
		getDao().save((D) this, args)
	}

	D persist() {
		getDao().save((D) this)
	}

	void remove() {
		getDao().delete((D) this)
	}

	@CompileDynamic
	@Override
	static List<D> list(Map params){
		println params
		CriteriaUtils.list(params.filters?:[:] as Map, this, params as Map)
	}

	@CompileDynamic
	@Override
	static List<D> list(Map filters, Map params){
		println params
		CriteriaUtils.list(filters as Map, this, params as Map)
	}

	@Deprecated
	static Map<String, Object> insertAndSave(Map params) {
		getDao().insert(params)
	}

	/**
	 * Creates, binds and persists and instance
	 * @return The created instance
	 */
	static Map<String, Object> create(Map params) {
		getDao().insert(params)
	}

	static Map<String, Object> update(Map params) {
		getDao().update(params)
	}

	static Map<String, Object> remove(Map params) {
		getDao().remove(params)
	}
}
