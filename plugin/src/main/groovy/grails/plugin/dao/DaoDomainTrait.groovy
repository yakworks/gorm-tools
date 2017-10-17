package grails.plugin.dao

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait DaoDomainTrait<D extends GormEntity> {
	static GormDaoSupport daoBean

	/**
	 * Looks up or creates and caches a dao bean
	 * @return The dao
	 */
	static GormDaoSupport getDao() {
		if(!daoBean) daoBean = DaoUtil.getDao(this)
		return daoBean
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
