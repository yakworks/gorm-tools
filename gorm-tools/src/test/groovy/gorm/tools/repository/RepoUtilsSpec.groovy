/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import org.springframework.util.ReflectionUtils

import gorm.tools.api.OptimisticLockingProblem
import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.api.EntityNotFoundProblem
import gorm.tools.support.MsgSourceResolvable
import gorm.tools.testing.unit.DataRepoTest
import grails.persistence.Entity

import org.springframework.dao.OptimisticLockingFailureException
import spock.lang.Specification
import testing.Cust
import yakworks.i18n.icu.ICUMessageSource

class RepoUtilsSpec extends Specification implements DataRepoTest {

    ICUMessageSource messageSource

    void setupSpec() {
        mockDomains MockDomain
    }

    void 'instanceControllersDomainBindingApi'() {
        expect:
        ReflectionUtils.findField(Cust, 'instanceControllersDomainBindingApi')
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
        def e = thrown(OptimisticLockingProblem)
        e.code

    }

    def "checkFound id number"() {

        when:
        RepoUtil.checkFound(null, 1, "Bloo")
        then:
        def e = thrown(EntityNotFoundProblem)
        e.code == 'error.notFound'
        e.message == "Bloo lookup failed using key [id:1]: code=error.notFound"

    }

    def "checkFound lookup is map"() {

        when:
        RepoUtil.checkFound(null, [code: 'abc'], "Bloo")
        then:
        def e = thrown(EntityNotFoundProblem)
        e.code == 'error.notFound'
        e.message == 'Bloo lookup failed using key [code:abc]: code=error.notFound'

    }



}

@Entity
class MockDomain {
    String name
}
