package gorm.tools.testing

import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.events.DaoEventInvoker
import gorm.tools.databinding.FastBinder
import grails.core.GrailsApplication
import grails.plugin.dao.DaoArtefactHandler
import org.springframework.util.ClassUtils

/**
 * Created by sudhir on 11/12/17.
 */
class DaoTestHelper {
    static GrailsApplication grailsApplication

    static Closure commonBeans() {
        return {
            fastBinder(FastBinder)
            daoEventInvoker(DaoEventInvoker)
            daoUtilBean(DaoUtil)
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
        } else {
            return DefaultGormDao
        }
    }

    static Closure registerDao(Class domain, Class daoClass) {
        String beanName = DaoUtil.getDaoBeanName(domain)
        grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
        if (daoClass == DefaultGormDao) {
            return {
                "$beanName"(daoClass, domain) { bean -> bean.autowire = true }
            }
        } else {
            return {
                "$beanName"(daoClass) { bean -> bean.autowire = true }
            }
        }
    }
}
