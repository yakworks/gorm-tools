/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.artefact.Artefact
import grails.buildtestdata.TestData
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

//import static grails.buildtestdata.TestData.build
import grails.plugin.gormtools.GormToolsPluginHelper
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.dao.OptimisticLockingFailureException
import spock.lang.Ignore
import testing.*

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Org,OrgExt, TestTrxRollback] }

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
        Org org = build(Org)

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
        when:
        Org org = build(Org)//new Org(name: "get_test_version").save()

        then: "version should be 0"
        org.version == 0

        when:
        org.name = "get_test_version_1"
        org.persist(flush: true)

        then: "version updated"
        org.version == 1

        when: "test get() with old version"
        Org.repo.get(org.id, 0)

        then:
        thrown(OptimisticLockingFailureException)

        when: "test get() with valid version"
        Org newOrg = Org.repo.get(org.id, 1)

        then:
        noExceptionThrown()
        1 == newOrg.version
        org.id == newOrg.id

        when: "check get() passing map of params"
        newOrg = Org.repo.get([id: org.id, version: 1])

        then:
        noExceptionThrown()
        1 == newOrg.version
        org.id == newOrg.id

    }

    @Ignore('needs to be looked at now with gorm 6.1.12')
    def "test dirty checking works for traits"() {
        when:
        Org org = build(Org)//new Org(name: "get_test_version").save()
        org.ext  = build(OrgExt, save:false)
        org.ext.org = org
        org.ext.save(failOnError: true, flush:true)
        //org.save(failOnError: true, flush:true)

        //RepoUtil.flush()

        then: "version should be 0"
        org.version == 1
        org.ext.version == 0
        !org.isDirty()
        !org.isDirty('ext')

        when: "changes happen to ext"
        org.ext.text1 = "make dirtysss"
        org.ext.text2 = "asfasfd"

        then: "Org and ext is dirty"
        //org.isDirty()
        org.isDirty('ext')
        org.ext.getDirtyPropertyNames() == ['text1']
        //org.ext.isDirty()
        org.getDirtyPropertyNames() == ['ext']

        when: "changes happen to ext"
        RepoUtil.flushAndClear()
        org = Org.get(3)
        assert org.name == 'name'
        org['name'] = "make dirty1"
        org.name2 = "make dirty2"
        //org.persist(flush: true)

        then: "name and name2 should be dirty"
        org.name == "make dirty1"
        org.isDirty()
        org.isDirty('name')
        org.getDirtyPropertyNames() == ['name','name2']
        org.getPersistentValue('name') == 'name'

    }

    def "test get with non-existent id"() {
        setup:
        Org org = build(Org)

        when:
        Org.repo.get(Org.last().id + 1, null)

        then:
        thrown EntityNotFoundException
    }

    def "test create with domain property"() {
        setup:
        def type = TestData.build(OrgType)
        Map params = [name: 'foo', type: type]

        when:
        Org org = Org.repo.create(params)

        then:
        org.name == "foo"
        org.type
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
        when:
        Org org = build(Org, save: false)
        Org.repo.persist(org)
        org = Org.get(org.id)

        then:
        org.name == "name"
        org.type
    }

    def "test persist with validation"() {
        when:
        Org.repo.persist(new Org(amount: 500))

        then:
        def e = thrown(EntityValidationException)
        e.message.contains("Field error in object 'testing.Org' on field 'name': rejected value [null]")
    }

    def "test update"() {
        when:
        Org org = build(Org)
        org.name = "test2"

        then:
        org.isDirty()
        org.id != null

        when:
        Map p = [id: org.id, name: 'foo']
        org = Org.repo.update(p, flush: true)

        then:
        org.name == "foo"
        Org.findByName("foo") != null
    }

    def "test update with non-existent id"() {
        when:
        Org.repo.update([name: 'foo', id: 99999999])

        then:
        thrown EntityNotFoundException
    }

    def "test remove"() {
        setup:
        Org org = build(Org)

        when:
        Org.repo.remove(org)

        then:
        Org.get(org.id) == null
    }

    def "test remove by Id"() {
        setup:
        Org org = build(Org)

        when:
        Org.repo.removeById([:], org.id)

        then:
        Org.get(org.id) == null
    }

    def "test remove by Id with non-existent id"() {
        when:
        Org.repo.removeById(99999999)

        then:
        thrown EntityNotFoundException
    }

    def "test bind"() {
        when:
        Org org = build(Org)
        Org.repo.bind(org, [name: "bind_test"], BindAction.Update)

        then:
        org.name == "bind_test"
    }

    def "test flush"() {
        setup:
        Org org = build(Org, name: 'test_flush')

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
        Org org = build(Org)

        expect:
        org.isAttached()

        when:
        Org.repo.clear()

        then:
        !org.isAttached()
    }

    def "test clear when update"() {
        setup:
        Org org = build(Org, name: 'test_clear')

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
        Org org = build(Org, name: 'test')

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

    @Ignore //FIXME this seems to work locally but not on circleCI
    def "test criteria name config"() {
        when:
        Org org = new Org(name: "test")

        then:
        org.repo.mangoQuery.criteriaKeyName == "testCriteriaName"
    }

    def "test default quick search fields"() {
        when:
        GormToolsPluginHelper.addQuickSearchFields(["name", "notExistingField"], getDatastore().mappingContext.persistentEntities as List<PersistentEntity>)

        then:
        TestTrxRollback.quickSearchFields == ["name"]

        when:
        TestTrxRollback.quickSearchFields = []
        GormToolsPluginHelper.addQuickSearchFields(["name", "notExistingField"], getDatastore().mappingContext.persistentEntities as List<PersistentEntity>)

        then:
        TestTrxRollback.quickSearchFields == ["name"]


    }
}

@Entity @GrailsCompileStatic
class TestTrxRollback {
    String name
    BigDecimal amount

    static constraints = {
        name blank: false, nullable: false
        amount nullable: true
    }
}

@Artefact("Repository")
class TestTrxRollbackRepo implements GormRepo<TestTrxRollback> {

    @Override
    TestTrxRollback doPersist(Map args, TestTrxRollback entity) {
        args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
        getRepoEventPublisher().doBeforePersist(this, entity, args)
        entity.save(args)
        getRepoEventPublisher().doAfterPersist(this, entity, args)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()

        return entity

    }

    @Override
    void doRemove(Map args, TestTrxRollback entity) {
        getRepoEventPublisher().doBeforeRemove(this, entity)
        entity.delete(args)
        getRepoEventPublisher().doAfterRemove(this, entity)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()
    }
}
