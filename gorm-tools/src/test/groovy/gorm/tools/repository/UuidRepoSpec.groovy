/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.repository.model.UuidGormRepo
import spock.lang.Specification
import testing.UuidSample
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.GormHibernateTest

class UuidRepoSpec extends Specification implements GormHibernateTest {

    //test the thing
    static List entityClasses = [UuidSample]

    def "assert proper repos are setup"() {
        expect:
        UuidSample.repo instanceof UuidGormRepo
        // UuidSample.repo instanceof IdGeneratorRepo
    }

    void "sanity check"() {
        when:
        def o = RepoTestData.build(UuidSample)

        then:
        o.id
    }

}
