/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileStatic

import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.core.env.PropertyResolver
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

import grails.config.Config
import spock.lang.AutoCleanup
import spock.lang.Shared
import yakworks.testing.gorm.support.BaseRepoEntityUnitTest
import yakworks.testing.gorm.support.RepoTestDataBuilder
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder from build-test-data plugin methods and adds in JsonViewSpecSetup
 * so that it possible to build json and map test data
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings(['Indentation'])
@CompileStatic
trait GormHibernateTest implements GrailsAppUnitTest, BaseRepoEntityUnitTest, RepoTestDataBuilder {
    //trait order above is important, GormToolsSpecHelper should come last as it overrides methods in GrailsAppUnitTest

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared PlatformTransactionManager transactionManager
    /** The transaction status, setup before each test */
    TransactionStatus transactionStatus

    /**
     * make sure it returns nothing?
     */
    @Override
    Class<?>[] getDomainClassesToMock() {
        return [] as Class<?>[]
    }

    void setupHibernate() {
        //setup the ConfigurationProperties beans
        // def cfgRegistry = (AnnotationConfigRegistry)ctx
        // cfgRegistry.register(BasicConfiguration)

        //Sets up the HibernateDatastore, reads the entityClasses static  and sets up whats returned there.
        initDatastore()
        //register some of the HibernateDatastore props as beans in the ctx.
        registerHibernateBeans()

        //read what was setup as persistentEntities in datastore, usally drive by the entityClasses static
        List domClasses = datastore.mappingContext.persistentEntities*.javaClass
        defineRepoBeans(domClasses as Class<?>[])
    }

    // @Override
    // @CompileDynamic
    // Closure hibernateBeans(){ { ->
    //     persistenceInterceptor(HibernatePersistenceContextInterceptor){
    //         hibernateDatastore = ref("hibernateDatastore")
    //     }
    // }}


    /**
     * Sets up the HibernateDatastore, reads the entityClasses and sets up whats returned there.
     */
    void initDatastore(){
        List persistentClasses = findEntityClasses()

        if (persistentClasses) {
            hibernateDatastore = new HibernateDatastore((PropertyResolver) config, persistentClasses as Class[])
        } else {
            String packageName = getPackageToScan(config)
            Package packageToScan = Package.getPackage(packageName) ?: getClass().getPackage()
            hibernateDatastore = new HibernateDatastore((PropertyResolver) config, packageToScan)
        }
        transactionManager = hibernateDatastore.getTransactionManager()
        assert transactionManager
    }

    void registerHibernateBeans(){
        ctx.beanFactory.registerSingleton("hibernateDatastore", hibernateDatastore)
        if (!ctx.containsBean("dataSource"))
            ctx.beanFactory.registerSingleton("dataSource", hibernateDatastore.getDataSource())
        if (!ctx.containsBean("grailsDomainClassMappingContext"))
            ctx.beanFactory.registerSingleton("grailsDomainClassMappingContext", hibernateDatastore.getMappingContext())
        if (!ctx.containsBean("persistenceInterceptor")){
            def pci = new HibernatePersistenceContextInterceptor()
            pci.hibernateDatastore = hibernateDatastore
            pci.sessionFactory = hibernateDatastore.sessionFactory
            ctx.beanFactory.registerSingleton("persistenceInterceptor", pci)
        }
    }

    // @AfterClass
    // void cleanupHibernateSpec() {
    //     AppCtx.setApplicationContext(null)
    // }
    //
    // @After
    // void cleanupAfterTest() {
    //     if (isRollback()) {
    //         transactionManager.rollback(transactionStatus)
    //     } else {
    //         transactionManager.commit(transactionStatus)
    //     }
    // }

    /**
     * @return the current session factory
     */
    SessionFactory getSessionFactory() {
        getDatastore().getSessionFactory()
    }

    /**
     * @return the current Hibernate session
     */
    Session getHibernateSession() {
        getSessionFactory().getCurrentSession()
    }

    /**
     * Whether to rollback on each test (defaults to true)
     */
    boolean isRollback() {
        return true
    }

    /**
     * Obtains the default package to scan
     *
     * @param config The configuration
     * @return The package to scan
     */
    String getPackageToScan(Config config) {
        config.getProperty('grails.codegen.defaultPackage', getClass().package.name)
    }

    /** consistency with other areas of grails and other unit tests */
    @Override
    HibernateDatastore getDatastore() {
        applicationContext.getBean(HibernateDatastore)
    }

    void autowire() {
        AutowireCapableBeanFactory beanFactory = applicationContext.autowireCapableBeanFactory
        beanFactory.autowireBeanProperties this, AutowireCapableBeanFactory.AUTOWIRE_NO, false
    }

}
