/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.hibernate

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugin.hibernate.support.HibernatePersistenceContextInterceptor

import gorm.tools.GormToolsBeanConfig
import gorm.tools.idgen.PooledIdGenerator
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.testing.support.GormToolsSpecHelper
import gorm.tools.testing.support.MockJdbcIdGenerator
import grails.buildtestdata.TestDataBuilder
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import yakworks.i18n.icu.GrailsICUMessageSource

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
        if (!ctx.containsBean("dataSource"))
            ctx.beanFactory.registerSingleton("dataSource", hibernateDatastore.getDataSource())
        if (!ctx.containsBean("grailsDomainClassMappingContext"))
            ctx.beanFactory.registerSingleton("grailsDomainClassMappingContext", hibernateDatastore.getMappingContext())
        if (!ctx.containsBean("persistenceInterceptor")){
            def pci = new HibernatePersistenceContextInterceptor()
            pci.hibernateDatastore = (HibernateDatastore)hibernateDatastore
            ctx.beanFactory.registerSingleton("persistenceInterceptor", pci)
        }

        defineBeans(new GormToolsBeanConfig(ctx).getBeanDefinitions())

        defineBeans{
            persistenceInterceptor(HibernatePersistenceContextInterceptor){
                hibernateDatastore = (HibernateDatastore)hibernateDatastore
            }
            jdbcIdGenerator(MockJdbcIdGenerator)
            idGenerator(PooledIdGenerator, ref("jdbcIdGenerator"))
            messageSource(GrailsICUMessageSource)
        }

        // defineBeans(doWithSpringFirst())

        //finds and register repositories for all the persistentEntities that got setup
        defineBeans {
            for(Class domainClass in datastore.mappingContext.persistentEntities*.javaClass){
                Class repoClass = findRepoClass(domainClass)
                grailsApplication.addArtefact(RepositoryArtefactHandler.TYPE, repoClass)
                String repoName = RepoUtil.getRepoBeanName(domainClass)
                if (repoClass == DefaultGormRepo) {
                    "$repoName"(repoClass, domainClass) { bean ->
                        bean.autowire = true
                    }
                } else {
                    "$repoName"(repoClass) { bean ->
                        bean.autowire = true
                    }
                }
            }
        }

        // doWithSpringAfter()
    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }


}
