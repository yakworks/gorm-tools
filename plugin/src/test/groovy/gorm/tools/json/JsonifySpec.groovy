package gorm.tools.json

import gorm.tools.testing.BuildEntityTester
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime

class JsonifySpec extends Specification implements BuildEntityTester<TestJsonifyDom> {

    void setupSpec(){
        //need to mock the JsonifyDomExt too as it won't automatically get picked up as its not required.
        mockDomains(JsonifyDomExt)
    }

    void "sanity check JsonifyDom build"() {
        when:
        entity

        then:
        entity
        !entity.ext

        when:
        def e2 = build(includes: ['ext'])

        then:
        e2.ext.nameExt
    }

    void "test render json view"() {
        when:
        def jsonResult = buildJson()

        then:
        jsonResult.json instanceof Map
        jsonResult.json.name == "name"
        jsonResult.json.id == 1
        !jsonResult.json.isActive
        jsonResult.jsonText == '{"id":1,"localDate":"2018-01-25","isActive":false,"date":"2018-01-26T01:36:02Z","name":"name","amount":0}'

    }

    void "test json includes association"() {
        when:
        def args = [deep:true, includes: ['name', 'ext', 'ext.id', 'ext.nameExt']]
        def result = Jsonify.render(getEntity(includes: ['ext']), args)

        then:
        result.jsonText == '{"ext":{"id":1,"nameExt":"nameExt"},"name":"name"}'

    }

    void "test json expand association"() {
        when:
        def args = [deep:true, includes: ['name', 'ext', 'ext.id', 'ext.nameExt']]
        def result = Jsonify.render(getEntity(includes: ['ext']), args)

        then:
        result.jsonText == '{"ext":{"id":1,"nameExt":"nameExt"},"name":"name"}'

    }

    void "test buildJson deep with *"() {
        when:
        def renderArgs = [deep:true, excludes:['ext.testJsonifyDom']]
        def result = buildJson(renderArgs, includes: '*' )

        then:
        result.json
        result.jsonText == '{"id":1,"localDate":"2018-01-25","ext":{"id":1,"nameExt":"nameExt"},"isActive":false,"date":"2018-01-26T01:36:02Z","secret":"secret","localDateTime":"2018-01-01T01:01:01","name":"name","amount":0}'
    }

    void "test build * for all fields"() {
        when:
        def org = TestJsonifyDom.build(includes:'*')

        then:
        org
    }

}

@Entity @GrailsCompileStatic
class TestJsonifyDom {
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
    static belongsTo = [testJsonifyDom: TestJsonifyDom]

    static mapping = {
        id generator:'foreign', params:[property:'testJsonifyDom']
        testJsonifyDom insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        nameExt nullable: false
    }
}
