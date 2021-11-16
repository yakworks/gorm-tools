/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import gorm.tools.api.EntityNotFoundProblem
import gorm.tools.repository.RepoUtil
import spock.lang.Specification

class ExceptionSpec extends Specification {

    def "assert proper repos are setup"() {

        when:
            RepoUtil.checkFound(null, 1, "Bla")
        then:
            def e = thrown(EntityNotFoundProblem)
            e.message == "code: error.notFound, detail: Lookup failed for Bla using data [id: 1]"

    }

}
