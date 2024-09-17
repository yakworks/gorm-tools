/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.databinding.BindAction
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.RepoEntity
import gorm.tools.utils.BenchmarkHelper
import grails.artefact.Artefact
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Specification
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.CustExt
import testing.CustRepo
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.NotFoundProblem
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

//import static grails.buildtestdata.TestData.build

class CrudApiSpec extends Specification implements GormHibernateTest {

    static entityClasses = [KitchenSink, SinkExt, SinkItem]

    @Autowired KitchenSinkRepo kitchenSinkRepo

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.createKitchenSinks(100)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    def "assert proper repos are setup"() {
        expect:
        KitchenSink.repo instanceof KitchenSinkRepo
        KitchenSink.repo.entityClass == KitchenSink
    }

}
