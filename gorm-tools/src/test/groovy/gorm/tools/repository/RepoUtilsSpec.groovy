/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import org.springframework.util.ReflectionUtils

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityNotFoundException
import grails.persistence.Entity
import grails.testing.gorm.DataTest
import org.springframework.dao.OptimisticLockingFailureException
import spock.lang.Specification
import testing.Org

class RepoUtilsSpec extends Specification implements DataTest {

    void 'instanceControllersDomainBindingApi'() {
        expect:
        ReflectionUtils.findField(Org, 'instanceControllersDomainBindingApi')
        //Org.hasProperty('instanceControllersDomainBindingApi')
    }

    void testCheckVersion() {
        when:
        def mocke = new MockDomain([name: "Billy"])
        mocke.version = 1
        mocke.errors = new EmptyErrors("empty")

        then:
        RepoUtil.checkVersion(mocke, 1)

        when:
        RepoUtil.checkVersion(mocke, 0)

        then:
        def e = thrown(OptimisticLockingFailureException)
        e.message == "Another user has updated the MockDomain while you were editing"

    }

    void "test checkFound"() {
        when:
        RepoUtil.checkFound(null, 99, 'xxx')

        then:
        EntityNotFoundException e = thrown(EntityNotFoundException)
        e.message == 'xxx not found with id 99'
    }

    void "test propName"() {
        when:
        String propname = RepoMessage.propName('xxx.yyy.ArDoc')

        then:
        propname == 'arDoc'
    }

    void "test notFound"() {
        when:
        Map r = RepoMessage.notFound("xxx.MockDomain", [id: "2"])

        then:
        r.code == "default.not.found.message"
        r.args == ["MockDomain", "2"]
        r.defaultMessage == "MockDomain not found with id 2"
    }

    void "test defaultLocale"() {
        when:
        Locale locale = RepoMessage.defaultLocale()

        then:
        locale == Locale.ENGLISH
    }

}

@Entity
class MockDomain {
    String name
}
