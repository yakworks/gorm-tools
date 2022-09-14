/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.config.PropertySourcesConfig
import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.springframework.boot.env.PropertySourceLoader
import org.springframework.core.env.PropertyResolver
import org.springframework.core.env.PropertySource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.DefaultTransactionAttribute

import gorm.tools.ConfigDefaults
import gorm.tools.repository.RepoLookup
import gorm.tools.validation.RepoValidatorRegistry
import grails.buildtestdata.TestDataBuilder
import grails.config.Config
import grails.testing.spring.AutowiredTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import yakworks.grails.GrailsHolder
import yakworks.spring.AppCtx
import yakworks.testing.gorm.support.GormToolsSpecHelper

/**
 * WORk IN PROGRESS REPLACEMENT FOR GormToolsHibernateSpec
 */
@SuppressWarnings(['UnnecessarySelfAssignment', 'Println', 'EmptyMethod', 'Indentation', 'UnusedPrivateMethod', 'InvertedIfElse'])
@CompileStatic
trait WIPGormHibernateSpec implements AutowiredTest, TestDataBuilder, GormToolsSpecHelper {

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared PlatformTransactionManager transactionManager

    @CompileDynamic
    void init() {
        //from original HibernateSpec
        doHibernateDatastore()

        RepoLookup.USE_CACHE = false
        //for some reason holder get scrambled so make sure it has the grailsApplication from this test
        GrailsHolder.setGrailsApplication(getGrailsApplication())
        AppCtx.setApplicationContext(getApplicationContext())

        if (!ctx.containsBean("dataSource"))
            ctx.beanFactory.registerSingleton("dataSource", hibernateDatastore.getDataSource())
        if (!ctx.containsBean("grailsDomainClassMappingContext"))
            ctx.beanFactory.registerSingleton("grailsDomainClassMappingContext", hibernateDatastore.getMappingContext())
        if (!ctx.containsBean("persistenceInterceptor")){
            def pci = new HibernatePersistenceContextInterceptor()
            pci.hibernateDatastore = (HibernateDatastore)hibernateDatastore
            ctx.beanFactory.registerSingleton("persistenceInterceptor", pci)
        }

        def beanClosures = [commonBeans(), hibernateBeans()]
        def doWithDomainsClosure = doWithDomains()
        if(doWithDomainsClosure) beanClosures.add(doWithDomainsClosure)

        //put here so we can use trait to setup security when needed
        def doWithSecurityClosure = doWithSecurity()
        if(doWithSecurityClosure) beanClosures.add(doWithSecurityClosure)

        defineBeansMany(beanClosures)
        // rescan needed after the beans are added
        ctx.getBean('repoEventPublisher').scanAndCacheEventsMethods()
        // doWithSpringAfter()
        RepoValidatorRegistry.init(hibernateDatastore, ctx.getBean('messageSource'))
        //put here so we can use trait to setup security when needed
        doAfterDomains()
    }

    void doHibernateDatastore(){
        def cfg = getConfig()

        // List<PropertySourceLoader> propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader())
        // ResourceLoader resourceLoader = new DefaultResourceLoader()
        // MutablePropertySources propertySources = new MutablePropertySources()
        // PropertySourceLoader ymlLoader = propertySourceLoaders.find { it.getFileExtensions().toList().contains("yml") }
        // if (ymlLoader) {
        //     load(resourceLoader, ymlLoader, "application.yml").each {
        //         propertySources.addLast(it)
        //     }
        // }
        // PropertySourceLoader groovyLoader = propertySourceLoaders.find { it.getFileExtensions().toList().contains("groovy") }
        // if (groovyLoader) {
        //     load(resourceLoader, groovyLoader, "application.groovy").each {
        //         propertySources.addLast(it)
        //     }
        // }
        // propertySources.addFirst(new MapPropertySource("defaults", getConfiguration()))
        // Config config = new PropertySourcesConfig(propertySources)

        List<Class> domainClasses = getDomainClasses()
        String packageName = getPackageToScan(config)

        if (!domainClasses) {
            Package packageToScan = Package.getPackage(packageName) ?: getClass().getPackage()
            hibernateDatastore = new HibernateDatastore((PropertyResolver) config, packageToScan)
        } else {
            hibernateDatastore = new HibernateDatastore((PropertyResolver) config, domainClasses as Class[])
        }
        transactionManager = hibernateDatastore.getTransactionManager()
    }

    @AfterClass
    static void cleanupAppCtx() {
        AppCtx.setApplicationContext(null)
    }

    /**
     * The transaction status
     */
    TransactionStatus transactionStatus

    @Before
    void initTran() {
        transactionStatus = transactionManager?.getTransaction(new DefaultTransactionAttribute())
    }

    @After
    void rollback() {
        if (isRollback()) {
            transactionManager?.rollback(transactionStatus)
        } else {
            transactionManager?.commit(transactionStatus)
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
     * @return The domain classes
     */
    List<Class> getDomainClasses() { [] }

    /**
     * Obtains the default package to scan
     *
     * @param config The configuration
     * @return The package to scan
     */
    String getPackageToScan(Config config) {
        config.getProperty('grails.codegen.defaultPackage', getClass().package.name)
    }

    private List<PropertySource> load(ResourceLoader resourceLoader, PropertySourceLoader loader, String filename) {
        if (canLoadFileExtension(loader, filename)) {
            Resource appYml = resourceLoader.getResource(filename)
            return loader.load(appYml.getDescription(), appYml) as List<PropertySource>
        } else {
            return Collections.emptyList()
        }
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
        return Arrays
            .stream(loader.fileExtensions)
            .map { String extension -> extension.toLowerCase() }
            .anyMatch { String extension -> name.toLowerCase().endsWith(extension) }
    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

    // @CompileStatic
    // Map getConfiguration() {
    //     Map cfg = ConfigDefaults.getConfigMap(false)
    //     def clos = doWithConfig()
    //     if (clos) {
    //         clos.call(cfg)
    //     }
    //     return cfg
    // }
    //
    @Override
    @CompileDynamic
    Closure doWithConfig() {
        { config ->
            gormConfigDefaults(config)
        }
    }

    PropertySourcesConfig gormConfigDefaults(PropertySourcesConfig config){
        config.putAll(ConfigDefaults.getConfigMap(false))
        return config
    }

}
