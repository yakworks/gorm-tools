/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
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
