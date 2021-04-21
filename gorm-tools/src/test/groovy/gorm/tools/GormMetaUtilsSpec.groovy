/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.utils.GormMetaUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.gorm.DomainUnitTest
import testing.Cust

class GormMetaUtilsSpec extends HibernateSpec implements DomainUnitTest<Cust> {

    List<Class> getDomainClasses() { [Cust] }

    static doWithSpring = {

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

}
