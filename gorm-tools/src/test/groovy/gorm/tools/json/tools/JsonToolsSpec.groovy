/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json.tools

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.json.JsonGenerator

import gorm.tools.beans.BeanPathTools
import gorm.tools.beans.EntityMapService
import gorm.tools.json.JsonTools
import gorm.tools.repository.model.RepoEntity
import gorm.tools.testing.TestTools
import gorm.tools.testing.unit.DomainRepoTest
import grails.buildtestdata.TestData
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.IgnoreRest
import spock.lang.Specification
import testing.Address
import testing.Cust
import testing.CustExt
import testing.CustType
import testing.Nested

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

class JsonifySpec extends Specification implements DomainRepoTest<Cust> {

    EntityMapService entityMapService

    void setupSpec(){
        //these won't automatically get picked up as thet are not required.
        mockDomains(CustExt, CustType, Nested, Address, JsonifyDom, JsonifyDomExt, NestedDom)
        //defineBeans(new JsonViewGrailsPlugin())
    }

    void "sanity check TestData.build"() {
        when:
        def org = build(includes: ['ext'])
        def org2 = build(includes: ['ext'])

        then:
        org.id == 1
        org.type.id == 1
        org.ext.text1
        org2.id == 2
    }

    void "test getIncludes"(){
        expect:
        List incRes = BeanPathTools.getIncludes("JsonifyDom", fields)
        result.size() == incRes.size()
        incRes.containsAll(incRes)

        where:
        fields                  | result
        ['id','name']           | ['id','name']
        ['id','name','ext']     | ['id','name','ext']
        ['id', 'company']       | ['id', 'company']
        ['*']                   | ['id', 'localDate', 'currency', 'isActive', 'date', 'name', 'secret', 'version', 'amount', 'name2', 'localDateTime']
        ['id','name', 'ext.*']  | ['id', 'name', 'ext.id', 'ext.nameExt', 'ext.version']

        ['id','name', 'ext.*', 'ext.nested.name']  | ['id', 'name', 'ext.id', 'ext.version', 'ext.nameExt', 'ext.nested.name']

    }

    @IgnoreRest
    void "test Org json stock"() {
        when:
        def org = build()
        def emap = entityMapService.createEntityMap(org, ['*'])
        def generator = new JsonGenerator.Options()
            .excludeNulls()
            .build()
        // def emap = [foo: 'bar']
        def res = generator.toJson(emap)

        then:
        res == '{"id":1,"inactive":false,"kind":"CLIENT","beforeValidateCheck":"got it","testIdent":"Num2","name":"name","type":{"id":1}}'

    }

    void "test JsonifyDom json stock"() {
        when: "no includes"
        def jdom = TestData.build([:], JsonifyDom)
        def res = JsonTools.render(jdom)

        then: 'should not include the ext because its null'
        res == '{"id":1,"localDate":"2018-01-25","isActive":false,"date":"2018-01-26T01:36:02Z","name":"name","amount":0}'

        when: "ext association is mocked up in data"
        jdom = TestData.build(JsonifyDom, includes: ['ext'])
        res = JsonTools.render(jdom)

        then: 'the default will be just the ext.id'
        res == '{"id":2,"localDate":"2018-01-25","ext":{"id":1},"isActive":false,"date":"2018-01-26T01:36:02Z","name":"name","amount":0}'

        when: "ext association is in includes and deep:true and not renderNulls"
        // jdom = TestData.build(JsonifyDom, includes: ['ext'])
        // def args = [includes: ['id', 'name', 'ext'], deep: true, renderNulls: true]
        //deep needs to be true
        def args = [includes: ['id', 'name', 'name2', 'ext.*'], deep: true]
        res = JsonTools.render(jdom, args)

        then: 'ext fields should be shown'
        res == '{"id":2,"ext":{"id":1,"nameExt":"nameExt"},"name":"name"}'


    }

    void "test JsonifyDom renderNulls: true"() {
        when: "ext association is in includes and deep:true and not renderNulls"
        def jdom = TestData.build(JsonifyDom, includes: ['ext'])
        def args = [includes: ['id', 'name', 'name2', 'ext.*'], deep: true, renderNulls: true]
        def res = Jsonify.render(jdom, args)

        then: 'ext fields should be shown'
        res.jsonText == '{"id":1,"ext":{"id":1,"nameExt":"nameExt"},"name":"name","name2":null}'

    }

    void "currency converter should work"() {
        when:
        def jdom = TestData.build(JsonifyDom, currency: Currency.getInstance('USD'))
        def args = [includes: ['id', 'currency']]
        def res = Jsonify.render(jdom, args)

        then:
        res.jsonText == '{"id":1,"currency":"USD"}'

    }

    void "transients should be rendered"() {
        when:
        def jdom = TestData.build(JsonifyDom, includes: ['ext'])
        def args = [includes: ['id', 'ext.nameExt', 'company'], deep: true]
        String incTrans = 'company'
        def res = Jsonify.render(jdom, args){ obj ->
            delegate.call(incTrans, obj[incTrans])
            //"$incTrans" obj[incTrans]
        }

        then:
        res.jsonText == '{"id":1,"ext":{"nameExt":"nameExt"},"company":"Tesla"}'

    }

    void "test BeanPathTools.buildMapFromPaths should be rendered"() {
        when:
        def jdom = TestData.build(JsonifyDom, includes: ['ext'])
        List inc = ['id', 'ext.nameExt', 'company']
        //def map = BeanPathTools.buildMapFromPaths(jdom, inc)
        def entityMapService = new EntityMapService()
        def emap = entityMapService.createEntityMap(jdom,inc)
        // def args = [includes: ['id', 'ext.nameExt', 'company'], deep: true]
        //String incTrans = 'company'
        def res = Jsonify.render(emap)

        then:
        res.jsonText == '{"id":1,"ext":{"nameExt":"nameExt"},"company":"Tesla"}'

    }

    void "test json includes association stock json-views"() {
        when:
        def org = build()
        def args = [includes: ['id', 'name', 'type.id']]
        def writer = Jsonify.renderWritable(org, args)

        then:
        writer.toString() == '{"id":1,"name":"name","type":{"id":1}}'

        when:
        def res = Jsonify.render(org, [includes: ['id', 'name', 'type.id']])

        then:
        res.jsonText == '{"id":1,"name":"name","type":{"id":1}}'
    }

    void "test list"() {
        when:
        def org = build()
        def org2 = build()
        def orgList = [org, org2]
        def result = Jsonify.render(orgList, [includes: ['name']])

        then:
        result.jsonText == '[{"name":"name"},{"name":"name"}]'
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
        result.jsonText == '{"id":1,"inactive":false,"kind":"CLIENT","beforeValidateCheck":"got it","testIdent":"Num2","name":"name"}'

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
        name        nullable: false, blank: false
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
