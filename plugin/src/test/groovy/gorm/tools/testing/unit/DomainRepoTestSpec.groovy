package gorm.tools.testing.unit

import spock.lang.Specification
import testing.Org
import testing.Project

class DomainRepoTestSpec extends Specification implements DomainRepoTest<Org> {

    void "verify repo is added"(){
        expect:
        entity.repo
    }

    void "verify build methods"(){
        expect:
        buildMap().name
        buildCreate().name
    }

}

