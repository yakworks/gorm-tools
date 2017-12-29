package gorm.tools.testing

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.MangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import grails.gorm.transactions.TransactionService
import grails.plugin.gormtools.RepositoryArtefactHandler
import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.testing.GrailsUnitTest
import org.junit.BeforeClass
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils

/**
 * Helper utils for mocking spring beans needed to test repository's and domains.
 *
 * @author Sudhir
 * @since 3.3.2
 */
trait GormToolsTestHelper extends GrailsUnitTest {

    @BeforeClass
    void setupTransactionService() {
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
            entityMapBinder(EntityMapBinder, grailsApplication)
            repoEventPublisher(RepoEventPublisher)
            repoUtilBean(RepoUtil)
            repoExceptionSupport(RepoExceptionSupport)
            mango(MangoQuery)
        }
    }

    /**
     * Finds repository class in same package as domain class.
     *
     * @param domainClass
     * @return
     */
    Class findRepoClass(Class domainClass) {
        String repoClassName = RepoUtil.getRepoClassName(domainClass)
        //println "finding $repoClassName"
        if (ClassUtils.isPresent(repoClassName, grailsApplication.classLoader)) {
            return ClassUtils.forName(repoClassName)
        }
        return DefaultGormRepo
    }

    Closure registerRepository(Class domain, Class repoClass) {
        String beanName = RepoUtil.getRepoBeanName(domain)
        grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
        Closure clos = { "$beanName"(repoClass) { bean -> bean.autowire = true } }

        if (repoClass == DefaultGormRepo) {
            clos = { "$beanName"(repoClass, domain) { bean -> bean.autowire = true } }
        }

        return clos
    }

    void flushAndClear(){
        getDatastore().currentSession.flush()
        getDatastore().currentSession.clear()
    }
}
