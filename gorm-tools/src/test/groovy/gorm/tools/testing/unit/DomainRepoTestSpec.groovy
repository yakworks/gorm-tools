/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import spock.lang.Specification
import testing.Cust

class DomainRepoTestSpec extends Specification implements DomainRepoTest<Cust> {

    void "verify repo is added"(){
        when:
        def o = build()

        then: "the repos for the main domain and its required property classes should have gotten setup"
        o.repo
        o.type.repo
    }

    void "verify build methods"(){
        expect:
        buildMap().name
    }

}
