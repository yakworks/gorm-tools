package gorm.tools.repository.errors

import gorm.tools.repository.RepoUtil
import spock.lang.Specification

class ExceptionSpec extends Specification {

    def "assert proper repos are setup"() {

        when:
            RepoUtil.checkFound(null, 1, "Bla")
        then:
            def e = thrown(EntityNotFoundException)
            e.message == "Bla not found with id 1"


    }



}
