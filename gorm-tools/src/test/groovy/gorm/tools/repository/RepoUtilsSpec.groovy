/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import org.springframework.util.ReflectionUtils

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.support.MsgSourceResolvable
import gorm.tools.testing.unit.DataRepoTest
import grails.persistence.Entity
import grails.testing.gorm.DataTest
import org.springframework.dao.OptimisticLockingFailureException
import spock.lang.Specification
import testing.Cust
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing
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
        def e = thrown(OptimisticLockingFailureException)
        e.message == "Another user has updated the MockDomain while you were editing"

    }

    void "test checkFound"() {
        when:
        RepoUtil.checkFound(null, 99, 'xxx')

        then:
        EntityNotFoundException e = thrown(EntityNotFoundException)
        e.code == 'default.not.found.message'
        e.message == 'xxx not found with id:99'
    }

    void "test propName"() {
        when:
        String propname = RepoMessage.propName('xxx.yyy.ArDoc')

        then:
        propname == 'arDoc'
    }

    void "test notFound"() {
        when:
        MsgSourceResolvable r = RepoMessage.notFoundId(MockDomain, 2)

        then:
        r.code == "default.not.found.message"
        r.args == ['MockDomain', 2]
        "MockDomain not found with id 2" == messageSource.getMessage(r.code, r.args)
        // r.defaultMessage == "MockDomain not found for id:2"
    }


}

@Entity
class MockDomain {
    String name
}
