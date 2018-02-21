package gorm.tools.testing.unit

import spock.lang.Specification
import testing.Org
import testing.OrgType

class DomainRepoTestSpec extends Specification implements DomainRepoTest<Org> {

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

