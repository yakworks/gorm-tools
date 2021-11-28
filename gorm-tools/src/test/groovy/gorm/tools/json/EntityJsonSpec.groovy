/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json

import java.time.LocalDate
import java.time.LocalDateTime

import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.repository.model.RepoEntity
import gorm.tools.testing.unit.DomainRepoTest
import grails.buildtestdata.TestData
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Ignore
import spock.lang.Specification
import testing.Address
import testing.Cust
import testing.CustExt
import testing.CustType
import testing.AddyNested

import static grails.gorm.hibernate.mapping.MappingBuilder.orm
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson

class EntityJsonSpec extends Specification implements DomainRepoTest<Cust> {

    MetaMapEntityService metaMapEntityService

    void setupSpec(){
        //these won't automatically get picked up as thet are not required.
        mockDomains(CustExt, CustType, AddyNested, Address, JsonifyDom, JsonifyDomExt, NestedDom)
        //defineBeans(new JsonViewGrailsPlugin())
    }

    void "test Org json stock"() {
        when:
        def org = build()
        def res = metaMapEntityService.toJson(org, ['*', 'type'])

        then:
        assertThatJson(res).isEqualTo('''{
            "id":1, "name":"name",
            "inactive":false, "kind":"CLIENT",
            "beforeValidateCheck":"got it",
            "testIdent":{"id":2,"name":"Num2"},
            "type":{"id":1}
        }''')

    }

    void "test JsonifyDom json stock"() {
        when: "no includes"
        def jdom = TestData.build([:], JsonifyDom)
        def res = metaMapEntityService.toJson(jdom)

        then: 'should not include the ext because its null'
        assertThatJson(res).isEqualTo('{"id":1,"localDate":"2018-01-25","isActive":false,"date":"2018-01-26T01:36:02Z","name":"name","amount":0}')

        when: "ext association is mocked up in data"
        jdom = TestData.build(JsonifyDom, includes: ['ext'])
        res = metaMapEntityService.toJson(jdom, ['*', 'ext'])

        then: 'the default will be just the ext.id'
        assertThatJson(res).isEqualTo('{"id":2,"localDate":"2018-01-25","ext":{"id":1},"isActive":false,"date":"2018-01-26T01:36:02Z","name":"name","amount":0}')

        when: "ext association is in includes with \$stamp"
        res = metaMapEntityService.toJson(jdom, ['id', 'name', 'ext.$stamp'])

        then: 'ext fields should be shown'
        assertThatJson(res).isEqualTo('{"id":2,"name":"name","ext":{"id":1,"nameExt":"nameExt"}}')

        when: "ext association is in includes and \$* should use stamp"
        res = metaMapEntityService.toJson(jdom, ['id', 'name', 'ext.$*'])

        then: 'ext fields should be shown'
        assertThatJson(res).isEqualTo('{"id":2,"name":"name","ext":{"id":1,"nameExt":"nameExt"}}')

    }

    void "currency converter should work"() {
        when:
        def jdom = TestData.build(JsonifyDom, currency: Currency.getInstance('USD'))
        def res = metaMapEntityService.toJson(jdom, ['id', 'currency'])

        then:
        res == '{"id":1,"currency":"USD"}'

    }

    void "transients should be rendered"() {
        when:
        def jdom = TestData.build(JsonifyDom, includes: ['ext'])
        def args = [includes: ['id', 'ext.nameExt', 'company'], deep: true]
        String incTrans = 'company'
        def res = metaMapEntityService.toJson(jdom, ['id', 'ext.nameExt', 'company'])

        then:
        res == '{"id":1,"ext":{"nameExt":"nameExt"},"company":"Tesla"}'

    }

    void "test list"() {
        when:
        def org = build()
        def org2 = build()
        def orgList = [org, org2]
        def result = metaMapEntityService.toJson(orgList, ['name'])

        then:
        result == '[{"name":"name"},{"name":"name"}]'
    }

    @Ignore //TODO excludes is no implemented
    void "test buildJson excludes"() {
        when:
        def org = build(includes: '*')
        //FIXME it should automtically exclude the ext.org since its the other side of the association
        def result =  metaMapEntityService.toJson(org, [excludes:['ext.org']])

        def expected = '''{
            id:1, name:'name', ext:[id:1, text1:'text1'],
            date:'2018-01-26T01:36:02Z', inactive:false, locDateTime:'2018-01-01T01:01:01',
            amount2:0, locDate:'2018-01-25', amount:0, name2:'name2'}'''

        then:
        assertThatJson(res).isEqualTo(expected)

    }

    void "test json includes ext.*"() {
        when:
        def org = build(includes: '*')
        def result = metaMapEntityService.toJson(org, ["name", 'ext.*'])

        then:
        assertThatJson(result).isEqualTo('{"ext":{"id":1,"text1":"text1","org":{"id":1}},"name":"name"}')

    }

}

@Entity @GrailsCompileStatic
class JsonifyDom implements RepoEntity<JsonifyDom> {
    String name
    String name2
    Boolean isActive = false
    BigDecimal amount
    Date date
    LocalDate localDate
    LocalDateTime localDateTime
    Currency currency
    String secret

    JsonifyDomExt ext

    static transients = ['company']

    String getCompany() {
        'Tesla'
    }

    static constraints = {
        name        nullable: false
        name2       nullable: true
        isActive    nullable: false
        amount      nullable: false
        date        nullable: false, example: "2018-01-26T01:36:02Z"
        localDate   nullable: false, example: "2018-01-25"

        currency nullable: true
        localDateTime nullable: true, example: "2018-01-01T01:01:01"
        secret   nullable: true, display: false
        ext      nullable: true
    }
}

@Entity @GrailsCompileStatic
class JsonifyDomExt {
    String nameExt
    NestedDom nested

    static belongsTo = [testJsonifyDom: JsonifyDom]

    static Map includes = [
        stamp: ['id', 'nameExt']  //picklist or minimal for joins
    ]

    static mapping = {
        id generator:'foreign', params:[property:'testJsonifyDom']
        testJsonifyDom insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        nameExt nullable: false
        nested nullable: true
    }
}

@Entity @GrailsCompileStatic
class NestedDom {
    String name

    static constraints = {
        name nullable: false
    }
    static mapping = orm {
        version false
    }
}
