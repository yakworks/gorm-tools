/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.hibernate

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate5.support.HibernatePersistenceContextInterceptor

import gorm.tools.plugin.GormToolsPluginHelper
import gorm.tools.testing.support.GormToolsSpecHelper
import gorm.tools.testing.support.JsonViewSpecSetup
import grails.buildtestdata.TestDataBuilder
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder from build-test-data plugin methods and adds in JsonViewSpecSetup
 * so that it possible to build json and map test data
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
abstract class GormToolsHibernateSpec extends HibernateSpec implements AutowiredTest, JsonViewSpecSetup, TestDataBuilder, GormToolsSpecHelper {

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

        Closure beans = {}

        beans = beans << {
            persistenceInterceptor(HibernatePersistenceContextInterceptor){
                hibernateDatastore = (HibernateDatastore)hibernateDatastore
            }
        }

        //finds and register repositories for all the persistentEntities that got setup
        datastore.mappingContext.persistentEntities*.javaClass.each { domainClass ->
            beans = beans << registerRepository(domainClass, findRepoClass(domainClass))
        }
        beans = beans << GormToolsPluginHelper.doWithSpring //commonBeans()
        beans = beans << doWithSpringFirst()
        defineBeans(beans)

    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

    /**
     * Call back to provide beans before repositories are mocked, this gives chance to define beans which may need to
     * be injected into repositories
     * @return
     */
    Closure doWithSpringFirst() {
        return {}
    }

}
