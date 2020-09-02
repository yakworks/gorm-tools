/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class CustomerSpec extends Specification implements DomainRepoTest<Customer> {

    void "crud tests"() {
        expect:
        testCreate()
        testUpdate()
        testPersist()
        testRemove()
    }

}
