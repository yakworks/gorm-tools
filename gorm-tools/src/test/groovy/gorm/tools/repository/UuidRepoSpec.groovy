/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.testing.RepoTestData
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.UuidSample
import yakworks.gorm.testing.model.Thing
import yakworks.gorm.testing.model.ThingRepo

class UuidRepoSpec extends GormToolsHibernateSpec {

    //test the thing
    List<Class> getDomainClasses() { [UuidSample] }

    def "assert proper repos are setup"() {
        expect:
        UuidSample.repo instanceof GormRepo
        // UuidSample.repo instanceof IdGeneratorRepo
    }

    void "sanity check"() {
        when:
        def o = RepoTestData.build(UuidSample)

        then:
        o.id
    }

}
