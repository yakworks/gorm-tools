/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm


import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.junit.BeforeClass
import org.springframework.context.annotation.AnnotationConfigRegistry
import org.springframework.core.env.PropertyResolver
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.DefaultTransactionAttribute

import grails.buildtestdata.TestDataBuilder
import grails.config.Config
import grails.testing.spring.AutowiredTest
import spock.lang.Shared
import spock.lang.Specification
import yakworks.spring.AppCtx
import yakworks.testing.gorm.support.BaseRepoEntityUnitTest
import yakworks.testing.grails.GrailsAppUnitTest
import yakworks.testing.grails.TestConfiguration

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder so that it possible to build json and map test data
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 * @deprecated use GormHibernateTest instead
 */
@Deprecated
@SuppressWarnings(['Indentation'])
@CompileStatic
abstract class GormToolsHibernateSpec extends Specification implements AutowiredTest, GrailsAppUnitTest, BaseRepoEntityUnitTest, TestDataBuilder {
    //trait order above is important, GormToolsSpecHelper should come last as it overrides methods in GrailsAppUnitTest

    private HibernateDatastore hibernateDatastore
    @Shared PlatformTransactionManager transactionManager
    /** The transaction status, setup before each test */
    TransactionStatus transactionStatus

    /**
     * Override this in test to tell what domainclasses is should mock up. Its not abstract to force it since the option exists to package scan
     */
    // List<Class> getDomainClasses() { [] }

    @BeforeClass
    void setupHibernate() {
        //setup the ConfigurationProperties beans
        def cfgRegistry = (AnnotationConfigRegistry)ctx
        cfgRegistry.register(TestConfiguration)

        //Sets up the HibernateDatastore, reads the getDomainClasses and sets up whats returned there.
        initDatastore()
        //register some of the HibernateDatastore props as beans in the ctx.
        registerHibernateBeans()

        //read what was setup as persistentEntities in datastore , see initDatastore for how it reads from getDomainClasses
        List domClasses = datastore.mappingContext.persistentEntities*.javaClass
        defineRepoBeans(domClasses as Class<?>[])
    }

    /**
     * DataTestSetupSpecInterceptor calls this and we dont want it on this one, so make sure it returns nothing
     */
    @Override
    Class<?>[] getDomainClassesToMock() {
        return [] as Class<?>[]
    }

    // @Override
    // @CompileDynamic
    // Closure hibernateBeans(){ { ->
    //     persistenceInterceptor(HibernatePersistenceContextInterceptor){
    //         hibernateDatastore = (HibernateDatastore)hibernateDatastore
    //     }
    // }}

    /**
     * Sets up the HibernateDatastore, reads the getDomainClasses and sets up whats returned there.
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
    }

    void registerHibernateBeans(){
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

    void cleanupSpec() {
        AppCtx.setApplicationContext(null)
    }

    void setup() {
        transactionStatus = transactionManager.getTransaction(new DefaultTransactionAttribute())
    }

    void cleanup() {
        if (isRollback()) {
            transactionManager.rollback(transactionStatus)
        } else {
            transactionManager.commit(transactionStatus)
        }
    }

    /**
     * @return the current session factory
     */
    SessionFactory getSessionFactory() {
        hibernateDatastore.getSessionFactory()
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
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

}
