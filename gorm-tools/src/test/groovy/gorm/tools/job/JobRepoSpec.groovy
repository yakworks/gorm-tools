/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.source.SourceType
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import testing.*

class JobRepoSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [JobImpl] }

    void "sanity check validation"() {
        setup:
        JSONElement testJson = new JSONArray(["One", "Two", "Three"])


        when:
        JobImpl job = JobImpl.create([sourceType: SourceType.ERP, sourceId: 'ar/org', data:testJson.toString().bytes])
        def isValid = job.validate()

        then:
        isValid
        job.id != null

        when:
        JobImpl j = JobImpl.get(job.id)
        String str = new String(j.data, "UTF-8")

        then:
        str == '["One","Two","Three"]'

    }

    void "sanity check validation with String as data"() {
        when:
        JobImpl job = JobImpl.create([id: 1, sourceType: SourceType.ERP, sourceId: 'ar/org', data:"blah blah".bytes])
        def isValid = job.validate()

        then:
        isValid
    }

}


