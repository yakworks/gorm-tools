/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.model.RepoEntity
import yakworks.testing.gorm.GormToolsHibernateSpec
import yakworks.testing.gorm.RepoTestData
import grails.artefact.Artefact
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.CustExt
import testing.CustRepo
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.NotFoundProblem

//import static grails.buildtestdata.TestData.build

class GormRepoSpec extends GormToolsHibernateSpec {

    static entityClasses = [Cust, CustExt, TestTrxRollback, KitchenSink, SinkExt, SinkItem]

    def "assert proper repos are setup"() {
        expect:
        Cust.repo instanceof CustRepo
        Cust.repo.entityClass == Cust
        Address.repo instanceof DefaultGormRepo
        Address.repo.entityClass == Address
        AddyNested.repo instanceof DefaultGormRepo
        AddyNested.repo.entityClass == AddyNested
    }

    def "test get"() {
        setup:
        KitchenSink ks = build(KitchenSink)

        when:
        KitchenSink newOrg = KitchenSink.repo.get(ks.id, null)

        then:
        null != newOrg
        ks.id == newOrg.id
        ks.name == newOrg.name

        when:
        newOrg = KitchenSink.repo.get(ks.id)

        then:
        null != newOrg
        ks.id == newOrg.id
        ks.name == newOrg.name
    }

    def "test get with version"() {
        when:
        KitchenSink sink = build(KitchenSink)//new Org(name: "get_test_version").save()

        then: "version should be 0"
        sink.version == 0

        when:
        sink.name = "get_test_version_1"
        sink.persist(flush: true)

        then: "version updated"
        sink.version == 1

        when: "test get() with old version"
        KitchenSink.repo.get(sink.id, 0)

        then:
        def ex = thrown(DataProblemException)
        ex.problem instanceof DataProblem
        ex.code == DataProblemCodes.OptimisticLocking.code

        when: "test get() with valid version"
        KitchenSink newOrg = KitchenSink.repo.get(sink.id, 1)

        then:
        noExceptionThrown()
        1 == newOrg.version
        sink.id == newOrg.id

    }

    def "test dirty checking works for traits"() {
        when:
        KitchenSink sink = build(KitchenSink)//new Org(name: "get_test_version").save()
        sink.ext  = build(SinkExt, save:false)
        sink.ext.kitchenSink = sink
        sink.ext.id = sink.id
        sink.ext.save(failOnError: true, flush:true)
        //org.save(failOnError: true, flush:true)

        //RepoUtil.flush()

        then: "version should be 0"
        sink.version == 1
        sink.ext.version == 0
        !sink.isDirty()
        !sink.isDirty('ext')

        when: "changes happen to ext"
        sink.ext.name = "make dirtysss"
        sink.ext.textMax = "as"

        then: "Org and ext is dirty"
        // org.isDirty()
        // org.isDirty('ext')
        sink.ext.getDirtyPropertyNames().containsAll(['name', 'textMax'])
        sink.ext.isDirty()
        // org.getDirtyPropertyNames() == ['ext']

        when: "changes happen to org"
        flushAndClear()
        KitchenSink sink2 = KitchenSink.get(sink.id)
        assert sink2.name == 'name'
        sink2['name'] = "make dirty1"
        sink2.name2 = "make dirty2"
        //org.persist(flush: true)

        then: "name and name2 should be dirty"
        sink2.name == "make dirty1"
        sink2.isDirty()
        sink2.isDirty('name')
        sink2.getDirtyPropertyNames() == ['name','name2']
        sink2.getPersistentValue('name') == 'name'

    }

    def "test get with non-existent id"() {
        setup:
        KitchenSink sink = build(KitchenSink)

        when:
        KitchenSink.repo.get(KitchenSink.last().id + 1, null)

        then:
        def prob = thrown(NotFoundProblem.Exception)

    }

    def "test create without required field"() {
        setup:
        Map params = [name: 'foo']

        when:
        Cust org = Cust.repo.create(params)

        then:
        def e = thrown(ValidationProblem.Exception)

        e.message.contains("Field error in object 'testing.Cust' on field 'type': rejected value [null]")
    }

    def "test persist"() {
        when:
        Cust org = build(Cust, save: false)
        Cust.repo.persist(org)
        org = Cust.get(org.id)

        then:
        org.name == "name"
        org.type
    }

    def "test persist with validation"() {
        when:
        Cust.repo.persist(new Cust(amount: 500))

        then:
        def e = thrown(ValidationProblem.Exception)
        e.message.contains("Field error in object 'testing.Cust' on field 'name': rejected value [null]")
    }

    def "test update"() {
        when:
        Cust org = build(Cust)
        org.name = "test2"

        then:
        org.isDirty()
        org.id != null

        when:
        Map p = [id: org.id, name: 'foo']
        org = Cust.repo.update(p, [flush: true])

        then:
        org.name == "foo"
        Cust.findByName("foo") != null
    }

    def "test update with non-existent id"() {
        when:
        Cust.repo.update([name: 'foo', id: 99999999])

        then:
        thrown NotFoundProblem.Exception
    }

    def "test remove"() {
        setup:
        Cust org = build(Cust)

        when:
        Cust.repo.remove(org)

        then:
        Cust.get(org.id) == null
    }

    def "test remove by Id"() {
        setup:
        Cust org = build(Cust)

        when:
        Cust.repo.removeById(org.id)

        then:
        Cust.get(org.id) == null
    }

    def "test remove by Id with non-existent id"() {
        when:
        Cust.repo.removeById(99999999)

        then:
        thrown NotFoundProblem.Exception
    }

    def "test bind"() {
        when:
        Cust org = build(Cust)
        Cust.repo.bind(org, [name: "bind_test"], BindAction.Update)

        then:
        org.name == "bind_test"
    }

    def "test flush"() {
        setup:
        Cust org = build(Cust, name: 'test_flush')

        expect:
        org.isAttached()
        Cust.findByName('test_flush') != null

        when:
        org.name = 'test_flush_updated'

        then:
        Cust.findByName('test_flush') != null
        Cust.findByName('test_flush_updated') == null

        when:
        Cust.repo.flush()
        assert org.isAttached()

        then:
        Cust.findByName('test_flush_updated') != null
        Cust.findByName('test_flush') == null
    }

    def "test clear"() {
        setup:
        Cust org = build(Cust)

        expect:
        org.isAttached()

        when:
        Cust.repo.clear()

        then:
        !org.isAttached()
    }

    def "test clear when update"() {
        setup:
        Cust org = build(Cust, name: 'test_clear')

        when:
        org.name = "test_clear_updated"

        then:
        org.isDirty()

        when:
        org.save()
        Cust.repo.clear()

        then:
        !org.isDirty()
        !org.isAttached()
        Cust.findByName("test_clear_updated") == null
        Cust.findByName("test_clear") != null
    }

    void "test persistToManyData"() {
        when:
        def ks = RepoTestData.build(KitchenSink)

        then:
        ks != null

        when:
        List<Map> items = [[name:"C1"], [name:"C2"]]
        List<SinkItem> result = KitchenSink.repo.persistToManyData(ks, SinkItem.repo, items, 'kitchenSink')

        then:
        result.size() == 2
        result[0].kitchenSink == ks
        result[0].name == "C1"
        result[1].kitchenSink == ks
        result[1].name == "C2"

    }

    def "test transaction rollback using withTrx"() {
        setup:
        Cust org = build(Cust, name: 'test')

        when:
        Cust.repo.withTrx {
            Cust newOrg = Cust.get(org.id)
            newOrg.name = "test_changed"
            newOrg.save()
            throw new RuntimeException()
        }

        then:
        thrown RuntimeException
        Cust.findByName("test_changed") == null

        when:
        Cust.repo.withTrx {
            Cust.repo.remove(org)
            throw new RuntimeException()
        }

        then:
        thrown RuntimeException
        Cust.findByName("test") != null
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
        // call save to bypass persist
        TestTrxRollback org = new TestTrxRollback(name: "test_update_withTrx").save(failOnError: true)
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

}

@Entity @GrailsCompileStatic
class TestTrxRollback implements RepoEntity<TestTrxRollback> {
    String name
    BigDecimal amount

    static constraints = {
        name nullable: false
        amount nullable: true
    }
}

@Artefact("Repository")
class TestTrxRollbackRepo implements GormRepo<TestTrxRollback> {

    @Override
    TestTrxRollback doPersist(TestTrxRollback entity, PersistArgs args) {
        getRepoEventPublisher().doBeforePersist(this, entity, args)
        entity.save(args as Map)
        getRepoEventPublisher().doAfterPersist(this, entity, args)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()

        return entity

    }

    @Override
    void doRemove(TestTrxRollback entity, PersistArgs args) {
        getRepoEventPublisher().doBeforeRemove(this, entity)
        entity.delete(args)
        getRepoEventPublisher().doAfterRemove(this, entity)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()
    }
}
