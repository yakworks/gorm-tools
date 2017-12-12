package gorm.tools.dao

import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.testing.DaoHibernateSpec
import grails.testing.spring.AutowiredTest
import testing.Location
import testing.Nested
import testing.Org

class GormDaoSpec extends DaoHibernateSpec implements AutowiredTest {

    DaoEventPublisher daoEventInvoker

    List<Class> getDomainClasses() { [Org,Location,Nested] }

    void setup() {
        //have to do this because when daoInvoker is registered dao artefacts are not available, TODO find better way
        //daoEventInvoker.cacheEventsMethods(OrgDao)
    }

    Closure doWithConfig() {{ config ->
        config.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
    }}


    def "test create"() {
        when:
        Map p = [name:'foo']
        p.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Org org = Org.dao.create(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on dao"
        org.event == "beforeCreate"
    }


    def "test update"() {
        given:
        Org org = new Org(name:"test")
        org.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        org.persist()

        expect:
        org.id != null

        when:
        Map p = [name:'foo', id:org.id]
        org = Org.dao.update(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on dao"
        org.event == "beforeUpdate"
    }

    def "test criteria name config"(){
        when:
        Org org = new Org(name:"test")

        then:
        org.dao.mangoQuery.criteriaKeyName == "testCriteriaName"
    }
}
