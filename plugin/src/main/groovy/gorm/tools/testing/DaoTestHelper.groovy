package gorm.tools.testing

import gorm.tools.TrxService
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.databinding.FastBinder
import gorm.tools.mango.MangoQuery
import grails.core.GrailsApplication
import grails.plugin.dao.DaoArtefactHandler
import org.springframework.util.ClassUtils

/**
 * Helper utils for mocking spring beans needed to test dao's and domains.
 *
 * @author Sudhir
 * @since 3.3.2
 */
class DaoTestHelper {
    static GrailsApplication grailsApplication

    static Closure commonBeans() {
        return {
            fastBinder(FastBinder)
            daoEventPublisher(DaoEventPublisher)
            daoUtilBean(DaoUtil)
            trxService(TrxService)
            mangoQuery(MangoQuery)
        }
    }

    /**
     * Finds dao class in same package as domain class.
     *
     * @param domainClass
     * @return
     */
    static Class findDaoClass(Class domainClass) {
        String daoClassName = DaoUtil.getDaoClassName(domainClass)
        String daoBeanName = DaoUtil.getDaoBeanName(domainClass)
        if (ClassUtils.isPresent(daoClassName, grailsApplication.classLoader)) {
            return ClassUtils.forName(daoClassName)
        }
        return DefaultGormDao
    }

    static Closure registerDao(Class domain, Class daoClass) {
        String beanName = DaoUtil.getDaoBeanName(domain)
        grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
        Closure clos = { "$beanName"(daoClass) { bean -> bean.autowire = true } }

        if (daoClass == DefaultGormDao) {
            clos = { "$beanName"(daoClass, domain) { bean -> bean.autowire = true } }
        }

        return clos
    }
}
