package gorm.tools.dao

import grails.core.GrailsApplication
import grails.plugin.dao.GormDaoSupport
import grails.util.GrailsNameUtils
import grails.util.Holders
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait DaoEntity<D extends GormEntity<D>> {

    private static GormDao daoBean

	/**
	 * Looks up and caches a dao bean
	 * @return The dao
	 */
	static GormDao<D> getDao() {
		if(!daoBean) {
			GrailsApplication grailsApplication = Holders.grailsApplication
			String domainName = GrailsNameUtils.getPropertyName(this.name)
			String daoName = "${domainName}Dao"
			daoBean = (GormDao<D>) grailsApplication.mainContext.getBean(daoName)
		}
		return daoBean
	}

	static void setDao(GormDao<D> dao) {
		daoBean = dao
	}

    D persist(Map args = [:]) {
        getDao().persist((D) this, args)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map params) {
        getDao().create(params)
    }

    static D update(Map params) {
        getDao().update(params)
    }

	void remove() {
		getDao().remove((D) this)
	}

    static void remove(Serializable id) {
        getDao().removeById(id)
    }
}
