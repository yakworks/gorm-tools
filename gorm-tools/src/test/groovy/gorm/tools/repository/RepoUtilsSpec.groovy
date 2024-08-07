/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.repository.errors.EmptyErrors
import grails.persistence.Entity
import org.springframework.util.ReflectionUtils
import spock.lang.Specification
import testing.Cust
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.NotFoundProblem
import yakworks.testing.gorm.unit.DataRepoTest

class RepoUtilsSpec extends Specification implements DataRepoTest {

    void setupSpec() {
        mockDomains MockDomain
    }

    void 'No instanceControllersDomainBindingApi'() {
        expect:
        !ReflectionUtils.findField(Cust, 'instanceControllersDomainBindingApi')
        //Org.hasProperty('instanceControllersDomainBindingApi')
    }

    void "checkVersion OptimisticLockingProblem"() {
        when:
        def mocke = new MockDomain([name: "Billy"])
        mocke.version = 1
        mocke.errors = new EmptyErrors("empty")

        then:
        RepoUtil.checkVersion(mocke, 1)

        when:
        RepoUtil.checkVersion(mocke, 0)

        then:
        def ex = thrown(DataProblemException)
        ex.problem instanceof DataProblem
        ex.code == DataProblemCodes.OptimisticLocking.code

    }

    def "checkFound id number"() {

        when:
        RepoUtil.checkFound(null, 1, "Bloo")
        then:
        def e = thrown(NotFoundProblem.Exception)
        e.code == 'error.notFound'
        e.message == "Bloo lookup failed using key [id:1]: code=error.notFound"

    }

    def "checkFound lookup is map"() {

        when:
        RepoUtil.checkFound(null, [code: 'abc'], "Bloo")
        then:
        def e = thrown(NotFoundProblem.Exception)
        e.code == 'error.notFound'
        e.message == 'Bloo lookup failed using key [code:abc]: code=error.notFound'

    }



}

@Entity
class MockDomain {
    String name
}
