/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.databinding.BindAction
import gorm.tools.repository.model.RepoEntity
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.artefact.Artefact
import grails.buildtestdata.TestData
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

//import static grails.buildtestdata.TestData.build

import org.springframework.dao.OptimisticLockingFailureException

import testing.*

class GormRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Cust, CustExt, TestTrxRollback, Project, ProjectChild] }

    def "assert proper repos are setup"() {
        expect:
        Cust.repo instanceof CustRepo
        Cust.repo.entityClass == Cust
        Address.repo instanceof DefaultGormRepo
        Address.repo.entityClass == Address
        Nested.repo instanceof DefaultGormRepo
        Nested.repo.entityClass == Nested
    }

    def "test get"() {
        setup:
        Cust org = build(Cust)

        when:
        Cust newOrg = Cust.repo.get(org.id, null)

        then:
        null != newOrg
        org.id == newOrg.id
        org.name == newOrg.name

        when:
        newOrg = Cust.repo.get(org.id)

        then:
        null != newOrg
        org.id == newOrg.id
        org.name == newOrg.name
    }

    def "test get with version"() {
        when:
        Cust org = build(Cust)//new Org(name: "get_test_version").save()

        then: "version should be 0"
        org.version == 0

        when:
        org.name = "get_test_version_1"
        org.persist(flush: true)

        then: "version updated"
        org.version == 1

        when: "test get() with old version"
        Cust.repo.get(org.id, 0)

        then:
        thrown(OptimisticLockingFailureException)

        when: "test get() with valid version"
        Cust newOrg = Cust.repo.get(org.id, 1)

        then:
        noExceptionThrown()
        1 == newOrg.version
        org.id == newOrg.id

    }

    def "test dirty checking works for traits"() {
        when:
        Cust org = build(Cust)//new Org(name: "get_test_version").save()
        org.ext  = build(CustExt, save:false)
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
        // org.isDirty()
        // org.isDirty('ext')
        org.ext.getDirtyPropertyNames() == ['text1', 'text2']
        org.ext.isDirty()
        // org.getDirtyPropertyNames() == ['ext']

        when: "changes happen to org"
        RepoUtil.flushAndClear()
        org = Cust.get(3)
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
        Cust org = build(Cust)

        when:
        Cust.repo.get(Cust.last().id + 1, null)

        then:
        thrown EntityNotFoundException
    }

    def "test create with domain property"() {
        setup:
        def type = TestData.build(CustType)
        Map params = [name: 'foo', type: type]

        when:
        Cust org = Cust.repo.create(params)

        then:
        org.name == "foo"
        org.type
    }

    def "test create without required field"() {
        setup:
        Map params = [isActive: true, amount: 10.0]

        when:
        Cust org = Cust.repo.create(params)

        then:
        def e = thrown(EntityValidationException)
        e.message.contains("Field error in object 'testing.Cust' on field 'name': rejected value [null]")
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
        def e = thrown(EntityValidationException)
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
        thrown EntityNotFoundException
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
        thrown EntityNotFoundException
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

    void "test doAssociation"() {
        when:
        Project p = Project.repo.create(name:"P1", testDate:"2017-01-01", isActive:"false", nested:[name: "Nested", value:"10.0"])

        then:
        p != null

        when:
        List<Map> childs = [[name:"C1"], [name:"C2"]]
        List<ProjectChild> result = Project.repo.doAssociation(ProjectChild, p, childs)

        then:
        result.size() == 2
        result[0].project == p
        result[0].name == "C1"
        result[1].project == p
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

}

@Entity @GrailsCompileStatic
class TestTrxRollback implements RepoEntity<TestTrxRollback> {
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
    TestTrxRollback doPersist(TestTrxRollback entity, Map args) {
        args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
        getRepoEventPublisher().doBeforePersist(this, entity, args)
        entity.save(args)
        getRepoEventPublisher().doAfterPersist(this, entity, args)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()

        return entity

    }

    @Override
    void doRemove(TestTrxRollback entity, Map args) {
        getRepoEventPublisher().doBeforeRemove(this, entity)
        entity.delete(args)
        getRepoEventPublisher().doAfterRemove(this, entity)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()
    }
}

@Entity @GrailsCompileStatic
class ProjectChild {
    String name
    Project project
}
