package gorm.tools.testing

import gorm.tools.dao.DaoUtil
import gorm.tools.dao.DefaultGormDao
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.databinding.GormMapBinder
import gorm.tools.mango.MangoQuery
import grails.gorm.transactions.TransactionService
import grails.plugin.dao.DaoArtefactHandler
import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.testing.GrailsUnitTest
import org.junit.BeforeClass
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils

/**
 * Helper utils for mocking spring beans needed to test dao's and domains.
 *
 * @author Sudhir
 * @since 3.3.2
 */
trait DaoTestHelper extends GrailsUnitTest {

    @BeforeClass
    void setupTransactionService() {
        DaoUtil.ctx = grailsApplication.mainContext
        //setup transactionService
        if (!ctx.containsBean("transactionService"))
            ctx.beanFactory.registerSingleton("transactionService", datastore.getService(TransactionService))
    }

    /**
     * FIX for https://github.com/grails/grails-testing-support/issues/22
     * changes dataStore to lowercase datastore for consistency
     */
    AbstractDatastore getDatastore() {
        getDataStore()
    }

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    Closure commonBeans() {
        return {
            gormMapBinder(GormMapBinder)
            daoEventPublisher(DaoEventPublisher)
            daoUtilBean(DaoUtil)
            //trxService(TrxService)
            mangoQuery(MangoQuery)
        }
    }

    /**
     * Finds dao class in same package as domain class.
     *
     * @param domainClass
     * @return
     */
    Class findDaoClass(Class domainClass) {
        String daoClassName = DaoUtil.getDaoClassName(domainClass)
        String daoBeanName = DaoUtil.getDaoBeanName(domainClass)
        if (ClassUtils.isPresent(daoClassName, grailsApplication.classLoader)) {
            return ClassUtils.forName(daoClassName)
        }
        return DefaultGormDao
    }

    Closure registerDao(Class domain, Class daoClass) {
        String beanName = DaoUtil.getDaoBeanName(domain)
        grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
        Closure clos = { "$beanName"(daoClass) { bean -> bean.autowire = true } }

        if (daoClass == DefaultGormDao) {
            clos = { "$beanName"(daoClass, domain) { bean -> bean.autowire = true } }
        }

        return clos
    }
}
