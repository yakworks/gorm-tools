package gorm.tools.testing.unit

import spock.lang.Specification
import testing.Org

class DomainRepoTestSpec extends Specification implements DomainRepoTest<Org> {

    void "verify repo is added"(){
        expect:
        build().repo
    }

    void "verify build methods"(){
        expect:
        buildMap().name
    }

}

