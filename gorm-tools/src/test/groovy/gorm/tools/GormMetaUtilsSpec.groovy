/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.utils.GormMetaUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.gorm.DomainUnitTest
import testing.Org

class GormMetaUtilsSpec extends HibernateSpec implements DomainUnitTest<Org> {

    List<Class> getDomainClasses() { [Org] }

    static doWithSpring = {

    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("testing.Org")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(new Org())
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    def "FindPersistentEntity"() {
        expect:
        GormMetaUtils.findPersistentEntity("Org")
        GormMetaUtils.findPersistentEntity("org")
        GormMetaUtils.findPersistentEntity("testing.Org")
    }

    def "getPersistentProperties"(){
        expect:
        GormMetaUtils.getPersistentProperties("testing.Org").size()
        GormMetaUtils.getPersistentProperties("testing.Org").find{it.name == "id"} != null
    }

}
