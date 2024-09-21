package gorm.tools.hibernate

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceContext

import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.InvalidDataAccessResourceUsageException

import gorm.tools.jdbc.DbDialectService
import gorm.tools.mango.jpql.JpqlQueryBuilder
import grails.gorm.DetachedCriteria
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Test for JPA and hibernate stuff
 */
class HibernateMetaModelSpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [KitchenSink]
    //@Autowired SessionFactory sessionFactory
    @Autowired DbDialectService dbDialectService
    @Autowired HibernateMappingContext grailsDomainClassMappingContext

    // EntityManagerFactory is the hibernate SessionFactory, not setup as a bean in the unit tests
    // hibernate session is the jpa entityManager
    //@Autowired EntityManagerFactory entityManagerFactory;


    void "beans setup"() {
        when:
        def names = ctx.beanDefinitionNames as List
        names.each {
            println it
        }
        then:
        dbDialectService.dialectName == 'h2'
        dbDialectService.isH2()
        grailsDomainClassMappingContext
    }

    void "playground for metamodel"() {
        when:
        def metmod = getSessionFactory().getMetamodel()
        metmod.entities.each { ent ->
            println ent.name
            ent.attributes.each { p ->
                println "  ${p.name} - ${p.javaType} - ${p.persistentAttributeType}"
            }
        }
        then:
        metmod

    }

}
