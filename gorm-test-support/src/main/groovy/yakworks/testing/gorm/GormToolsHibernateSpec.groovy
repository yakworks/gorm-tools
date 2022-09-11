/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor

import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.validation.RepoValidatorRegistry
import grails.buildtestdata.TestDataBuilder
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import yakworks.grails.GrailsHolder
import yakworks.i18n.icu.GrailsICUMessageSource
import yakworks.spring.AppCtx
import yakworks.testing.gorm.support.ExternalConfigLoader
import yakworks.testing.gorm.support.GormToolsSpecHelper
import yakworks.testing.gorm.support.MockJdbcIdGenerator

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder from build-test-data plugin methods and adds in JsonViewSpecSetup
 * so that it possible to build json and map test data
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
abstract class GormToolsHibernateSpec extends HibernateSpec implements AutowiredTest, TestDataBuilder, GormToolsSpecHelper {

    //@OnceBefore
    void setupSpec() {
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

        // defineBeans(new GormToolsBeanConfig(ctx).getBeanDefinitions())

        //finds and register repositories for all the persistentEntities that got setup
        Closure beanClos = {
            persistenceInterceptor(HibernatePersistenceContextInterceptor){
                hibernateDatastore = (HibernateDatastore)hibernateDatastore
            }
            jdbcIdGenerator(MockJdbcIdGenerator)
            idGenerator(PooledIdGenerator, ref("jdbcIdGenerator"))
            messageSource(GrailsICUMessageSource)
            externalConfigLoader(ExternalConfigLoader)

            for(Class domainClass in datastore.mappingContext.persistentEntities*.javaClass){
                Class repoClass = findRepoClass(domainClass)
                grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
                String repoName = RepoUtil.getRepoBeanName(domainClass)
                if (repoClass == DefaultGormRepo || repoClass == UuidGormRepo) {
                    "$repoName"(repoClass, domainClass)
                } else {
                    "$repoName"(repoClass)
                }
            }
        }

        def beanClosures = [commonBeans(), beanClos]
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

    void cleanupSpec() {
        AppCtx.setApplicationContext(null)
    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

}
