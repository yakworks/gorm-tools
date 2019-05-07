/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json

import gorm.tools.testing.TestDataJson
import gorm.tools.testing.TestTools
import gorm.tools.testing.unit.DomainRepoTest
import grails.buildtestdata.TestData
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Ignore
import spock.lang.Specification
import testing.Location
import testing.Org
import testing.OrgExt

import java.time.LocalDate
import java.time.LocalDateTime

class JsonifySpec extends Specification implements DomainRepoTest<Org> {

    void setupSpec(){
        //these won't automatically get picked up as thet are not required.
        mockDomains(OrgExt, Location)
    }

    void "sanity check TestData.build"() {
        when:
        def org = build(includes: ['ext'])

        then:
        org.type.id == 1
        org.ext.text1
    }

    void "test json includes association stock json-views"() {
        when:
        def org = build()
        def args = [deep:true, includes: ['id', 'name', 'type', 'type.id']]
        def result = Jsonify.render(org, args)

        then:
        result.jsonText == '{"id":1,"id":1,"name":"name","type":{"id":1}}'
    }

    @Ignore
    void "test type.id should get expanded"() {
        when:
        def org = build()
        //FIXME type.id does not work. the following should pass
        def result = Jsonify.render(org, [includes: ['name', 'type.id']])

        then:
        result.jsonText == '{"type":{"id":1},"name":"name"}'
    }

    void "test buildJson deep"() {
        when:
        def org = build(includes: '*')
        //FIXME it should automtically exclude the ext.org since its the other side of the association
        def result =  Jsonify.render(org, [deep:true, excludes:['ext.org']])

        def expected = [id:1, name:'name', ext:[id:1, text1:'text1'],
                        date:'2018-01-26T01:36:02Z', inactive:false, locDateTime:'2018-01-01T01:01:01',
                        amount2:0, locDate:'2018-01-25', amount:0, name2:'name2']
        then:
        TestTools.mapContains(result.json, expected)
    }

    void "test json includes with *"() {
        when:
        def args = [deep:true, includes: ["*"]]
        def result = Jsonify.render(build(), [includes: ["*"]])

        then: //TODO: double id issue
        result.jsonText == '{"id":1,"id":1,"inactive":false,"name":"name"}'

    }

    void "test json includes ext.*"() {
        when:
        def args = [deep:true, includes: ["name", "ext.*"]]
        def org = build(includes: '*')
        def result = Jsonify.render(org, args)

        then:
        result.jsonText == '{"ext":{"id":1,"text1":"text1"},"name":"name"}'

    }

}

@Entity @GrailsCompileStatic
class JsonifyDom {
    String name
    Boolean isActive = false
    BigDecimal amount
    Date date
    LocalDate localDate
    LocalDateTime localDateTime
    //Currency currency
    String secret

    JsonifyDomExt ext

    static constraints = {
        name        nullable: false, blank: false
        isActive    nullable: false
        amount      nullable: false
        date        nullable: false, example: "2018-01-26T01:36:02Z"
        localDate   nullable: false, example: "2018-01-25"

        //currency nullable: true
        localDateTime nullable: true, example: "2018-01-01T01:01:01"
        secret   nullable: true, display: false
        ext      nullable: true
    }
}

@Entity @GrailsCompileStatic
class JsonifyDomExt {
    String nameExt
    static belongsTo = [testJsonifyDom: JsonifyDom]

    static mapping = {
        id generator:'foreign', params:[property:'testJsonifyDom']
        testJsonifyDom insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        nameExt nullable: false
    }
}
