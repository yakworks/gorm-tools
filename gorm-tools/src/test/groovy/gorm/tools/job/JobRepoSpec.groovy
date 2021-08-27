/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.source.SourceType
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.*

class JobRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [JobImpl] }

    void "sanity check validation"() {
        when:
        JobImpl job = JobImpl.create([id: 1, sourceType: SourceType.ERP, sourceId: 'ar/org', data:"blah blah".toByte])
        def isValid = job.validate()

        then:
        isValid
    }

    void "sanity check validation with String as data"() {
        when:
        JobImpl job = JobImpl.create([id: 1, sourceType: SourceType.ERP, sourceId: 'ar/org', data:"blah blah"])
        def isValid = job.validate()

        then:
        isValid
    }

}


