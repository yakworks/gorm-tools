/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import java.time.LocalDateTime

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.taskify.repo.ContactRepo

class ContactSpec extends Specification implements DomainRepoTest<Contact>, SecurityTest {

    //ContactRepo and Contact are in different packages
    @IgnoreRest
    void "verify repo is found"() {
        expect:
        Contact.repo instanceof ContactRepo
    }

    void "CRUD tests"() {
        expect:
        createEntity().id
        persistEntity().id
        updateEntity().version > 0
        removeEntity()
    }

}
