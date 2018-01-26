package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.plugin.gormtools.GormToolsPluginHelper
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.dao.OptimisticLockingFailureException
import testing.*

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org, TestTrxRollback, Location, Nested, Company] }

    Closure doWithConfig() {
        { config ->
            config.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
        }
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
        def e = thrown(OptimisticLockingFailureException)
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
        Location location = new Location(id:1, city: "City", nested: new Nested(name: "Nested", value: 1)).save()
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
        Org.repo.flush()

        then:
        org.name == "foo"
        Org.findByName("foo") != null
        org.location == location
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

    def "test remove"() {
        setup:
        Org org = new Org(name: "test_update_withTrx").save()

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
        thrown EntityNotFoundException
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

    def "test flush"() {
        setup:
        Org org = new Org(name: 'test_flush').save()

        expect:
        org.isAttached()
        Org.findByName('test_flush') != null

        when:
        org.name = 'test_flush_updated'

        then:
        Org.findByName('test_flush') != null
        Org.findByName('test_flush_updated') == null

        when:
        Org.repo.flush()
        assert org.isAttached()
        then:
        Org.findByName('test_flush_updated') != null
        Org.findByName('test_flush') == null
    }

    def "test clear"() {
        setup:
        Org org = new Org(name: 'test_clear').save()

        expect:
        org.isAttached()

        when:
        Org.repo.clear()

        then:
        !org.isAttached()
    }

    def "test clear when update"() {
        setup:
        Org org = new Org(name: 'test_clear').save()

        when:
        org.name = "test_clear_updated"

        then:
        org.isDirty()

        when:
        org.save()
        Org.repo.clear()

        then:
        !org.isDirty()
        !org.isAttached()
        Org.findByName("test_clear_updated") == null
        Org.findByName("test_clear") != null
    }

    def "test transaction rollback using withTrx"() {
        setup:
        Org org = new Org(name: "test").save()

        when:
        Org.repo.withTrx {
            Org newOrg = Org.get(org.id)
            newOrg.name = "test_changed"
            newOrg.save()
            throw new RuntimeException()
        }

        then:
        thrown RuntimeException
        Org.findByName("test_changed") == null

        when:
        Org.repo.withTrx {
            Org.repo.remove(org)
            throw new RuntimeException()
        }

        then:
        thrown RuntimeException
        Org.findByName("test") != null
    }

    def "test persist with transaction rollback"() {
        setup:
        TestTrxRollback org = new TestTrxRollback(name: "test_persist_withTrx").save()

        when:
        org.name = "changed"

        /* persist is overridden in TestTrxRollbackRepo and throws a runtime exception.
         * persist contains withTrx {} inside, so transaction should rollback */
        TestTrxRollback.repo.persist(org)

        then:
        thrown RuntimeException
        TestTrxRollback.findByName("changed") == null

    }

    def "test update with transaction rollback"() {
        setup:
        TestTrxRollback org = new TestTrxRollback(name: "test_update_withTrx").save()
        Map params = [name: 'foo', id: org.id]
        TestTrxRollback.repo.clear()

        expect:
        !org.isAttached()

        when:
        /* persist is overridden in TestTrxRollbackRepo and throws a runtime exception.
         * update contains withTrx {} inside, so transaction should rollback */
        TestTrxRollback.repo.update(params)

        then:
        thrown RuntimeException
        TestTrxRollback.findByName("test_update_withTrx") != null
        TestTrxRollback.findByName("foo") == null
    }

    def "test remove with transaction rollback"() {
        setup:
        TestTrxRollback org = new TestTrxRollback(name: "test_remove_withTrx").save()
        TestTrxRollback.repo.clear()

        expect:
        !org.isAttached()

        when:
        /* remove is overridden in TestTrxRollbackRepo and throws a runtime exception.
         * remove contains withTrx {} inside, so transaction should rollback */
        TestTrxRollback.repo.remove(org)

        then:
        thrown RuntimeException
        TestTrxRollback.findByName("test_remove_withTrx") != null
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
