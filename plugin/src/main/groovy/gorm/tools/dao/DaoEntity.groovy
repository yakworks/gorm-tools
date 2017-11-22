package gorm.tools.dao

import grails.core.GrailsApplication
import grails.plugin.dao.GormDaoSupport
import grails.util.GrailsNameUtils
import grails.util.Holders

trait DaoEntity<D> {

    private static GormDaoSupport daoBean

    D persist(Map args = [:]) {
        getDao().save((D) this, args)
    }

    void remove() {
        getDao().delete((D) this)
    }

    /**
     * Looks up and caches a dao bean
     * @return The dao
     */
    static GormDao<D> getDao() {
        if(!daoBean) {
            GrailsApplication grailsApplication = Holders.grailsApplication
            String domainName = GrailsNameUtils.getPropertyName(this.name)
            String daoName = "${domainName}Dao"
            daoBean = (GormDaoSupport<D>) grailsApplication.mainContext.getBean(daoName)
        }
        return daoBean
    }

    static void setDao(GormDao<D> dao) {
        daoBean = dao
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

    static void remove(Serializable id) {
        getDao().remove(id)
    }
}
