package grails.plugin.dao

import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

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
		String domainName = GrailsNameUtils.getPropertyName(this.name)
		String daoName = "${domainName}Dao"
		(GormDaoSupport<D>)grailsApplication.mainContext.getBean(daoName)
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
