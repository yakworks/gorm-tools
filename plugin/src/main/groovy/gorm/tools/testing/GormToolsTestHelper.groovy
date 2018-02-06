package gorm.tools.testing

import gorm.tools.TrxService
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.MangoQuery
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import grails.plugin.gormtools.RepositoryArtefactHandler
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.testing.GrailsUnitTest
import org.junit.BeforeClass
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils

/**
 * Helper utils for mocking spring beans needed to test repository's and domains.
 */
@CompileDynamic
//@SuppressWarnings(["ClosureAsLastMethodParameter"])
trait GormToolsTestHelper extends GrailsUnitTest {

    @BeforeClass
    void setupTransactionService() {
        //if (!ctx.containsBean("transactionService"))
        //    ctx.beanFactory.registerSingleton("transactionService", datastore.getService(TransactionService))
        defineBeans {
            trxService(TrxService)
        }
    }

    /**
     * Mocks Repositories for passed in Domain classes.
     * If a Repository Class is explicitly defined then this looks for it in the same package
     * The domains should be mocked before this is called
     */
    void mockRepositories(Class<?>... domainClassesToMock) {
        Closure repoBeans = {}

        domainClassesToMock.each { Class domainClass ->
            Class repoClass = findRepoClass(domainClass)
            repoBeans = repoBeans << registerRepository(domainClass, repoClass)
        }
        //check again for transactionService, for some reason it doesn't get picked up in @OnceBefore
        //if (!ctx.containsBean("transactionService"))
        //    ctx.beanFactory.registerSingleton("transactionService", datastore.getService(TransactionService))

        defineBeans(repoBeans << commonBeans())
    }

    /**
     * FIX for https://github.com/grails/grails-testing-support/issues/22
     * changes dataStore to lowercase datastore for consistency
     */
    @CompileDynamic
    AbstractDatastore getDatastore() {
        getDataStore()
    }

    /** conveinince shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    @CompileDynamic
    Closure commonBeans() {
        return {
            entityMapBinder(EntityMapBinder, grailsApplication)
            repoEventPublisher(RepoEventPublisher)
            repoUtilBean(RepoUtil)
            repoExceptionSupport(RepoExceptionSupport)
            mango(MangoQuery)
            trxService(TrxService)
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

    @CompileDynamic
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
