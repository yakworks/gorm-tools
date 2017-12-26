package gorm.tools.repository

import gorm.tools.testing.GormToolsHibernateSpec
import gorm.tools.repository.errors.*
import gorm.tools.databinding.BindAction
import org.grails.datastore.gorm.*
import grails.plugin.gormtools.GormToolsPluginHelper
import org.grails.datastore.mapping.model.PersistentEntity
import org.hibernate.ObjectNotFoundException
import testing.Company
import testing.Location
import testing.Nested
import testing.Org
import testing.Org2

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org, Org2, Location, Nested, Company] }

    Closure doWithConfig() {
        { config ->
            config.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
        }
    }

    def setup() {
        new Org(name: "test_from_setup").save(flush: true)
    }

    def "assert proper repos are setup"() {
        expect:
        Org.repo instanceof DefaultGormRepo
        Org.repo.entityClass == Org
        Location.repo instanceof DefaultGormRepo
        Location.repo.entityClass == Location
        Nested.repo instanceof DefaultGormRepo
        Nested.repo.entityClass == Nested
    }

    def "test get"() {
        setup:
        Org org = new Org(name: "get_test").save()

        when:
        Org newOrg = Org.repo.get(org.id, null)

        then:
        null != newOrg
        org.id == newOrg.id
        org.name == newOrg.name

        when:
        newOrg = Org.repo.get([id: org.id])

        then:
        null != newOrg
        org.id == newOrg.id
        org.name == newOrg.name
    }

    def "test get with version"() {
        setup:
        Org org = new Org(name: "get_test_version").save()
        org.name = "get_test_version_1"
        org.save(flush: true)

        when: "test get() with valid version"
        Org newOrg = Org.repo.get(org.id, 1L)

        then:
        noExceptionThrown()
        1L == newOrg.version
        org.id == newOrg.id

        when: "check get() passing map of params"
        newOrg = Org.repo.get([id: org.id, version: 1L])

        then:
        noExceptionThrown()
        1L == newOrg.version
        org.id == newOrg.id

        when: "test get() with lower version"
        Org.repo.get(org.id, 0L)

        then:
        def e = thrown(EntityValidationException)
        e.message.contains("Another user has updated the Org while you were editing")
    }

    def "test get with non-existent id"() {
        setup:
        Org org = new Org(name: "test").save()

        when:
        Org.repo.get(Org.last().id + 1, null)

        then:
        thrown EntityNotFoundException
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

//        and: "Event should have been fired on repository"
//        org.event == "beforeBind Create"
    }

    def "test create without required field"() {
        setup:
        Map params = [isActive: true, amount: 10.0]

        when:
        Org org = Org.repo.create(params)

        then:
        def e = thrown(EntityValidationException)
        e.message.contains("Field error in object 'testing.Org' on field 'name': rejected value [null]")
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

    def "test persist with validation"() {
        when:
        Org.repo.persist(new Org(amount: 500))

        then:
        def e = thrown(EntityValidationException)
        e.message.contains("Field error in object 'testing.Org' on field 'name': rejected value [null]")
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

//        and: "Event should have been fired on repository"
//        org.event == "beforeBind Update"
    }

    def "test update with non-existent id"() {
        setup:
        Org org = new Org(name: "test").save()
        Map params = [name: 'foo', id: Org.last().id + 1]

        when:
        Org.repo.update(params)

        then:
        thrown EntityNotFoundException
    }

    @spock.lang.Ignore
    def "test update with transaction"() {
        setup:
        Org2 org = new Org2(name: "test").save()
        Map params = [name: 'foo', id: org.id]

        when:
        Org2.repo.update(params)
        RepoUtil.clear()

        then:
        thrown RuntimeException
        org.name == "test"
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

    def "test remove by Id with non-existent id"() {
        setup:
        new Org(name: '1').save()

        when:
        Org.repo.removeById([:], Org.last().id + 1)

        then:
        thrown ObjectNotFoundException
    }

    def "test bind"() {
        setup:
        Map data = [name: "bind_test"]
        Org org = new Org(name: "test").save()

        expect:
        org.name == "test"

        when:
        Org.repo.bind(org, data, BindAction.Update)

        then:
        org.name == "bind_test"
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
