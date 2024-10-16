/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import gorm.tools.beans.EntityResult
import gorm.tools.databinding.BindAction
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.model.RepoEntity
import grails.artefact.Artefact
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Specification
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.CustExt
import testing.CustRepo
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.NotFoundProblem
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

//import static grails.buildtestdata.TestData.build

class GormRepoUpsertSpec extends Specification implements GormHibernateTest {

    static entityClasses = [KitchenSink, SinkExt, SinkItem]

    def "test UPSERT"() {
        when:
        EntityResult k = KitchenSink.repo.upsert([id:123, num: '123', name: "k123"],PersistArgs.withBindId())
        var k2 = KitchenSink.get(123)

        then: "CREATED"
        k.entity
        k.status == HttpStatus.CREATED
        k2

        when: "same one is passed in again"
        flushAndClear()
        k = KitchenSink.repo.upsert([id:123, num: '123', name: "updated"])
        flushAndClear()
        k2 = KitchenSink.get(123)

        then:
        k.entity.name == "updated"
        k.status == HttpStatus.OK
        k2.name == "updated"
    }

    void "test upsert"() {
        when: "data has no identifier"
        def ks = KitchenSink.repo.upsert([num: 1, name: "foo"]).entity
        flushAndClear()

        then:
        KitchenSink.get(ks.id)

        when: "data has identifier"
        flushAndClear()
        def ksu = KitchenSink.repo.upsert([id: ks.id, name: "foo2"]).entity
        flushAndClear()

        def updated = KitchenSink.get(ksu.id)

        then:
        updated.name == "foo2"
    }
}
