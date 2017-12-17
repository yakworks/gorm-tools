package gorm.tools.repository

import gorm.tools.testing.GormToolsHibernateSpec
import testing.Location
import testing.Nested
import testing.Org
import testing.OrgRepo

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org, Location, Nested] }

    Closure doWithConfig() {
        { config ->
            config.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
        }
    }

    def "assert proper repos are setup"() {
        expect:
        Org.repo instanceof OrgRepo
        Location.repo instanceof DefaultGormRepo
        Location.repo.domainClass == Location
        Nested.repo instanceof DefaultGormRepo
        Nested.repo.domainClass == Nested
    }


    def "test create"() {
        when:
        Map p = [name: 'foo']
        p.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Org org = Org.repo.create(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on repository"
        org.event == "beforeCreate"
    }


    def "test update"() {
        given:
        Org org = new Org(name: "test")
        org.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        org.persist()

        expect:
        org.id != null

        when:
        Map p = [name: 'foo', id: org.id]
        org = Org.repo.update(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on repository"
        org.event == "beforeUpdate"
    }

    def "test criteria name config"() {
        when:
        Org org = new Org(name: "test")

        then:
        org.repo.mangoQuery.criteriaKeyName == "testCriteriaName"
    }
}
