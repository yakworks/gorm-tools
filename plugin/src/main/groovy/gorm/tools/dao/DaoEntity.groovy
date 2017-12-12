package gorm.tools.dao

import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

import javax.persistence.Transient

@CompileStatic
trait DaoEntity<D extends GormEntity<D>> {

    @Transient
    private static DaoApi _cachedDao

    /**
     * Looks up and caches a dao bean
     * @return The dao
     */
    static DaoApi<D> getDao() {
        if(!_cachedDao) {
            GrailsApplication grailsApplication = Holders.grailsApplication
            String domainName = GrailsNameUtils.getPropertyName(this.name)
            String daoName = "${domainName}Dao"
            _cachedDao = grailsApplication.mainContext.getBean(daoName, DaoApi)
        }
        return _cachedDao
    }

    static void setDao(DaoApi<D> dao) {
        _cachedDao = dao
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
