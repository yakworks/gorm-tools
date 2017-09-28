package grails.plugin.dao

import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait DaoDomainTrait<D extends GormEntity> {

	static GormDaoSupport<D> getDao() {
		GrailsApplication grailsApplication = Holders.grailsApplication
		String domainName = this.name
		Class domainClass = grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, domainName).clazz
		String daoName = "${GrailsNameUtils.getPropertyName(domainName)}Dao"
		GormDaoSupport<D> dao
		if (grailsApplication.mainContext.containsBean(daoName)) {
			dao = (GormDaoSupport<D>) grailsApplication.mainContext.getBean(daoName)
		} else {
			dao = (GormDaoSupport) grailsApplication.mainContext.getBean("gormDaoBean")
			dao.domainClass = domainClass
		}
		if (!dao) {
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

	static Map<String, Object> insertAndSave(Map params) {
		getDao().insert(params)
	}

	static Map<String, Object> update(Map params) {
		getDao().update(params)
	}

	static Map<String, Object> remove(Map params) {
		getDao().remove(params)
	}
}
