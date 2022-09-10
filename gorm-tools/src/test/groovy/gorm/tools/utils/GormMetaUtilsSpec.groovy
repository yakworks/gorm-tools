/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import yakworks.testing.gorm.GormToolsHibernateSpec
import testing.Cust
import testing.CustType

class GormMetaUtilsSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Cust, CustType] }

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

}
