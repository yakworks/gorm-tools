/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.events.DefaultApplicationEventPublisher
import org.grails.datastore.gorm.utils.ClasspathEntityScanner
import org.grails.datastore.mapping.core.connections.ConnectionSources
import org.grails.datastore.mapping.core.connections.ConnectionSourcesInitializer
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.connections.HibernateConnectionSourceFactory
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.core.env.PropertyResolver
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

import gorm.tools.jdbc.DbDialectService
import spock.lang.AutoCleanup
import spock.lang.Shared
import yakworks.commons.lang.PropertyTools
import yakworks.testing.gorm.TestTools
import yakworks.testing.gorm.support.BaseRepoEntityUnitTest
import yakworks.testing.gorm.support.RepoTestDataBuilder
import yakworks.testing.grails.GrailsAppUnitTest
import yakworks.testing.grails.SpringBeanUtils

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder from build-test-data plugin methods
 * helpers to build json and map test data
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
        HibernateConnectionSourceFactory hcsf
        if (persistentClasses) {
            hcsf = new HibernateConnectionSourceFactory(persistentClasses as Class[])
        } else {
            List entityPackages = (PropertyTools.getOrNull(this, 'entityPackages')?:[]) as List
            List<Package> packagesToScan = entityPackages.collect{ grailsApplication.classLoader.getDefinedPackage(it as String) }
            hcsf = new HibernateConnectionSourceFactory(new ClasspathEntityScanner().scan(packagesToScan as Package[]) )
        }

        //FIXME wont work with Environment, but passing in `config` tests are not picking up the naming_strategy
        // when passing in env then its not picking up the gorm methods like .list() etc..
        // its the HibernteSettings not getting setup right.
        // set breakpoint on 228 HibernateConnectionSourceFactory to see problems with config
        // see GrailsApplicationPostProcessor L118,

        //ConfigurableEnvironment env =  this.applicationContext.getEnvironment()
        //TestTools.addEnvConverters(env)
        // env.propertySources.addFirst(ConfigDefaults.propertySource)
        // env.propertySources.addFirst(new MapPropertySource("grails", config.getProperties()) )
        //env.propertySources.addFirst(ConfigDefaults.propertySource)

        //PropertyResolver propEnv = DatastoreUtils.preparePropertyResolver(env, "dataSource", "hibernate", "grails")

        TestTools.addConfigConverters(config)

        //config.put('hibernate.naming_strategy', DefaultNamingStrategy)
        ConnectionSources connectionSources = ConnectionSourcesInitializer.create(hcsf, config as PropertyResolver)
        def mapCtx = hcsf.getMappingContext()
        def daep = new DefaultApplicationEventPublisher()
        hibernateDatastore = new HibernateDatastore(connectionSources, mapCtx, daep)

        // works fine with the grails config.
        // hibernateDatastore = new HibernateDatastore(config, hcsf, daep)

        transactionManager = hibernateDatastore.getTransactionManager()
        assert transactionManager
    }


    void registerHibernateBeans(){
        BeanDefinitionRegistry bdr = (BeanDefinitionRegistry)ctx
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
        def springBeans = [
            dbDialectService: DbDialectService,
            jdbcTemplate: [JdbcTemplate, "@dataSource"]
        ]
        SpringBeanUtils.registerBeans((BeanDefinitionRegistry)ctx, springBeans)
        //
        // BeanDefinition bdJdbcTemplate = BeanDefinitionBuilder.rootBeanDefinition(JdbcTemplate)
        //         .addConstructorArgReference("dataSource").getBeanDefinition()
        // bdr.registerBeanDefinition("jdbcTemplate", bdJdbcTemplate)
        //
        // BeanDefinition bdDbDialectService = BeanDefinitionBuilder.rootBeanDefinition(DbDialectService).getBeanDefinition()
        // bdr.registerBeanDefinition("dbDialectService", bdDbDialectService)

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
     */
    // String getPackageToScan(Config config) {
    //     config.getProperty('grails.codegen.defaultPackage', getClass().package.name)
    // }

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
