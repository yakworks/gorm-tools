/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import spock.lang.Specification
import testing.Cust
import testing.CustType
import testing.UuidSample
import yakworks.testing.gorm.unit.GormHibernateTest

class GormMetaUtilsSpec extends Specification implements GormHibernateTest {

    static List entityClasses = [Cust, CustType, UuidSample]

    void setupSpec(){
        new CustType(name: 'foo').persist(flush: true)
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("testing.Cust")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(new Cust())
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Cust)
    }

    def "FindPersistentEntity"() {
        expect:
        GormMetaUtils.findPersistentEntity("Cust")
        GormMetaUtils.findPersistentEntity("cust")
        GormMetaUtils.findPersistentEntity("testing.Cust")
    }

    def "getPersistentProperties"(){
        expect:
        GormMetaUtils.getPersistentProperties("testing.Cust").size()
        GormMetaUtils.getPersistentProperties("testing.Cust").find{it.name == "id"} != null
    }

    void "test getMetaProperties"() {
        when:
        List<MetaProperty> metaProps = GormMetaUtils.getMetaProperties(CustType)

        then:
        metaProps.size() == 3
        metaProps.find { it.name == 'id' }
        metaProps.find { it.name == 'version' }
        metaProps.find { it.name == 'name' }
        !metaProps.find { it.name == 'constraintsMap' }

    }

    void "test getProperties"() {
        when:
        Map custTypeMap = GormMetaUtils.getProperties(CustType.get(1))

        then:
        custTypeMap == [id:1, version:0, name: 'foo']
    }

    void "test isNewOrDirty"() {
        when:
        CustType ctNew = new CustType(name: 'foo')
        UuidSample ctNewUuid = new UuidSample()
        CustType ctDirty = new CustType(name: "foo").persist()
        ctDirty.name = 'dirty'

        then:
        GormMetaUtils.isNewOrDirty(ctNew)
        GormMetaUtils.isNewOrDirty(ctNewUuid)
        GormMetaUtils.isNewOrDirty(ctDirty)

        when: "id is assigned it will still show as new"
        ctNew.id = 99
        UuidSample.repo.generateId(ctNewUuid)

        then:
        ctNew.id
        ctNewUuid.id
        GormMetaUtils.isNewOrDirty(ctNew)
        GormMetaUtils.isNewOrDirty(ctNewUuid)

        when: "version is assigned its no longer new"
        // ctNew.persist()
        // ctNewUuid.persist()
        ctNew.version = 0
        ctNewUuid.version = 0
        //trackChanges resets the tracking.
        ctNew.trackChanges()
        ctNewUuid.trackChanges()

        then:
        !ctNew.hasChanged()
        !GormMetaUtils.isNewOrDirty(ctNew)
        !GormMetaUtils.isNewOrDirty(ctNewUuid)
    }

    void "test getEntityClass"() {
        when:
        CustType type = CustType.load(999) //doesnt matter, it returns a proxy and thts what we want to verify

        then: "its a proxy"
        type.getClass().name.contains('$')
        type.getClass().name != 'testing.CustType'


        when:
        Class clazz = GormMetaUtils.getEntityClass(type)

        then:
        clazz.name == "testing.CustType"

    }

}
