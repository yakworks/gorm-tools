/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools


import groovy.transform.CompileStatic

import gorm.tools.json.Jsonify
import gorm.tools.testing.unit.DomainRepoTest
import gorm.tools.traits.IdEnum
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Rollback
import grails.persistence.Entity
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class JsonifySpec extends Specification {

    @Ignore
    void "test json includes with *"() {
        when:
        def args = [deep:true, includes: ["*"]]
        def result = Jsonify.render(new JsonifyTestDom(), [includes: ["*"]])

        then: //TODO: double id issue
        result.jsonText == '{"id":1,"inactive":false,"name":"name"}'

    }

}

@Entity @GrailsCompileStatic
class JsonifyTestDom {
    String name
    TestIdent testIdent = TestIdent.Num2

    static constraints = {
        name nullable: false, blank: false
    }
}

@CompileStatic
enum TestIdent implements IdEnum<TestIdent,Long> {
    Num2(2), Num4(4)
    final Long id

    TestIdent(Long id) { this.id = id }
}
