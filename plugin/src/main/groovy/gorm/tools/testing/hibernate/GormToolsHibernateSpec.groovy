package gorm.tools.testing.hibernate

import gorm.tools.testing.unit.GormToolsSpecHelper
import gorm.tools.testing.unit.JsonViewSpecSetup
import grails.buildtestdata.TestDataBuilder
import grails.plugin.gormtools.GormToolsPluginHelper
import grails.test.hibernate.HibernateSpec
import grails.testing.spock.OnceBefore
import groovy.transform.CompileDynamic
import org.grails.datastore.mapping.core.AbstractDatastore

/**
 * Can be a drop in replacement for the HibernateSpec. Makes sure repositories are setup for the domains
 * and incorporates the TestDataBuilder from build-test-data plugin methods and adds in JsonViewSpecSetup
 * so that it possible to build json and map test data
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod'])
@CompileDynamic
abstract class GormToolsHibernateSpec extends HibernateSpec implements JsonViewSpecSetup, TestDataBuilder, GormToolsSpecHelper {

    @OnceBefore
    void setupRepoBeans() {
        if (!ctx.containsBean("dataSource"))
            ctx.beanFactory.registerSingleton("dataSource", hibernateDatastore.getDataSource())
        if (!ctx.containsBean("grailsDomainClassMappingContext"))
            ctx.beanFactory.registerSingleton("grailsDomainClassMappingContext", hibernateDatastore.getMappingContext())

        Closure beans = {}

        //finds and register repositories for all the persistentEntities that got setup
        datastore.mappingContext.persistentEntities*.javaClass.each { domainClass ->
            beans = beans << registerRepository(domainClass, findRepoClass(domainClass))
        }
        beans = beans << GormToolsPluginHelper.doWithSpring //commonBeans()
        defineBeans(beans)
    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

}
