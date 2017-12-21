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
        when:
        Map p = [name: 'foo']
        p.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        Org org = Org.repo.create(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on repository"
        org.event == "beforeBind Create"
    }


    def "test update"() {
        given:
        Org org = new Org(name: "test")
        org.location = new Location(city: "City", nested: new Nested(name: "Nested", value: 1)).save()
        org.persist()
        org.name = "test2"

        expect:
        org.isDirty()
        org.id != null

        when:
        Map p = [name: 'foo', id: org.id]
        org = Org.repo.update(p)

        then:
        org.name == "foo"

        and: "Event should have been fired on repository"
        org.event == "beforeBind Update"
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
