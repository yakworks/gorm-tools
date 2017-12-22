package gorm.tools.repository

import gorm.tools.testing.GormToolsHibernateSpec
import grails.plugin.gormtools.GormToolsPluginHelper
import org.grails.datastore.mapping.model.PersistentEntity
import testing.Company
import testing.Location
import testing.Nested
import testing.Org
import testing.OrgRepo

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org, Location, Nested, Company] }

    Closure doWithConfig() {
        { config ->
            config.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
        }
    }

    def "assert proper repos are setup"() {
        expect:
        Org.repo instanceof OrgRepo
        Location.repo instanceof DefaultGormRepo
        Location.repo.entityClass == Location
        Nested.repo instanceof DefaultGormRepo
        Nested.repo.entityClass == Nested
    }


    def "test create"() {
        setup:
        Location location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Map params = [name: 'foo', location: location]

        when:
        Org org = Org.repo.create(params)

        then:
        org.name == "foo"
        org.location.city == location.city
        org.location.nested == location.nested

        and: "Event should have been fired on repository"
        org.event == "beforeBind Create"
    }

    def "test persist"() {
        setup:
        Location location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()

        when:
        Org org = new Org(name: 'foo', location: location)
        Org.repo.persist(org)
        org = Org.get(org.id)

        then:
        org.name == "foo"
        org.location.city == location.city
        org.location.nested == location.nested
    }

    def "test update"() {
        setup:
        Location location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Org org = new Org(name: "test")
        org.persist()
        org.name = "test2"

        expect:
        org.isDirty()
        org.id != null
        org.location == null

        when:
        Map p = [name: 'foo', id: org.id, location: location]
        org = Org.repo.update(p)

        then:
        org.name == "foo"
        org.location == location

        and: "Event should have been fired on repository"
        org.event == "beforeBind Update"
    }

    def "test remove"() {
        setup:
        Org org = new Org(name: "test").save()

        when:
        Org.repo.remove(org)

        then:
        Org.get(org.id) == null
    }

    def "test remove by Id"() {
        setup:
        Org org = new Org(name: "test2").save()

        when:
        Org.repo.removeById([:], org.id)

        then:
        Org.get(org.id) == null
    }

    def "test criteria name config"() {
        when:
        Org org = new Org(name: "test")

        then:
        org.repo.mangoQuery.criteriaKeyName == "testCriteriaName"
    }

    def "test default quick search fields"() {
        when:
        GormToolsPluginHelper.addQuickSearchFields(["name", "num", "notExistingField"], getDatastore().mappingContext.persistentEntities as List<PersistentEntity>)

        then:
        Company.quickSearchFields == ["name", "num"]

        when:
        Company.quickSearchFields = []
        GormToolsPluginHelper.addQuickSearchFields(["name", "location.city", "notExistingField"], getDatastore().mappingContext.persistentEntities as List<PersistentEntity>)

        then:
        Company.quickSearchFields == ["name", "location.city"]
    }
}
