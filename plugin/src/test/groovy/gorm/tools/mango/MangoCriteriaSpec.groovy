package gorm.tools.mango

import gorm.tools.Address
import gorm.tools.hibernate.criteria.DynamicCriteriaBuilder
import grails.core.GrailsDomainClassProperty
import grails.persistence.Entity
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import spock.lang.Specification

@Domain([Org,Location,Nested])
@TestMixin(HibernateTestMixin)
class MangoCriteriaSpec extends Specification {

    def setup() {
    }

    def "test detached isActive"() {
        setup:
        (1..10).each { index ->
            String value = "Name#" + index
            new Org(id: index,
                    name: value,
                    isActive: (index % 2 == 0 ),
                    amount: (index-1)*1.34,
                    amount2: (index-1)*(index-1)*0.3,
                    date: new Date().clearTime() + index,
                    secondName: index % 2 == 0 ? null :  "Name2#" + index,
                    location: (new Location(city: "City#$index", nested: new Nested(name: "Nested#${2*index}", value: index)).save())
            ).save(failOnError: true)
        }
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([isActive:true])).list()

        then:
        res.size() == 5
    }

    def "test detached string"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([name: "Name#1"])).list()

        then:
        res.size() == 1
    }

    def "test detached like"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([name: "Name#%"])).list()

        then:
        res.size() == 10
    }

    def "test combined"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: [1*1.34, 2*1.34, 3*1.34, 4*1.34], isActive:true])).list()

        then:
        res.size() == 2
    }

    def "test detached BigDecimal"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: 1.34])).list()

        then:
        res.size() == 1

        when:
        res = dcb.build(MangoTidyMap.tidy([amount: ['$gt':6.0]])).list()

        then:
        res.size() == 5
    }

    def "test detached Date"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([date: new Date().clearTime() + 2])).list()

        then:
        res.size() == 1

        when:
        res = dcb.build(MangoTidyMap.tidy([date: ['$gt': new Date().clearTime() + 7]])).list()

        then:
        res.size() == 3
    }

    def "test gt"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([id: ['$gt':4]])).list()

        then:
        res.size() == 6
    }

    def "test ne"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([id: ['$ne':4]])).list()

        then:
        res.size() == 9
    }

    def "test nested"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(["location.id": ['$eq':6]])).list()

        then:
        res.size() == 1
    }

    def "test nested String"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(["location.city": "City#4"])).list()

        then:
        res.size() == 1
    }


 def "test nestedId"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(["locationId": ['$eq':6]])).list()

        then:
        res.size() == 1
    }


    def "test or"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy('$or':[[name: "Name#7"], [id:2]])).list()

        then:
        res.size() == 2
    }

    def "test not in list"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: ['$nin':[1*1.34, 2*1.34, 3*1.34, 4*1.34]]])).list()

        then:
        res.size() == 6
    }

    def "test in list"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([id: [1,2,3,4]])).list()

        then:
        res.size() == 4
    }

    def "test not"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(['$not': [[id:['$eq':1]]]])).list()

        then:
        res.size() == 9
    }


    def "test between"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: ['$between':[1*1.34, 4*1.34]]])).list()

        then:
        res.size() == 4
    }

    def "test isNull/ isNotNull"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([secondName: ['$isNull': true]])).list()

        then:
        res.size() == 5

        when:

        res = dcb.build(MangoTidyMap.tidy([secondName: '$isNull'])).list()

        then:
        res.size() == 5
    }

    def "test fields comparison"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: ['$gtef':"amount2"]])).list()

        then:
        res.size() == 5

        when:
        res = dcb.build(MangoTidyMap.tidy([amount: ['$gtf':"amount2"]])).list()

        then:
        res.size() == 4

        when:
        res = dcb.build(MangoTidyMap.tidy([amount: ['$ltf':"amount2"]])).list()

        then:
        res.size() == 5

        when:
        res = dcb.build(MangoTidyMap.tidy([amount: ['$eqf':"amount2"]])).list()

        then:
        res.size() == 1

        when:
        res = dcb.build(MangoTidyMap.tidy([amount: ['$nef':"amount2"]])).list()

        then:
        res.size() == 9
    }

    def "test quickSearch"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(['$quickSearch': "Name#%"])).list()

        then:
        res.size() == 10

        when:

        res = dcb.build(MangoTidyMap.tidy(['$quickSearch': "Name#3"])).list()

        then:
        res.size() == 1

        when:
        res = dcb.build(MangoTidyMap.tidy(['$quickSearch': "Name#%", isActive: true])).list()

        then:
        res.size() == 5


    }

    def "test with closure"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([name: "Name#%"])){gt "id", 5}.list()

        then:
        res.size() == 5
    }


    def "test with deep nested"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(["location.nested.name": "Nested#4"])).list()

        then:
        res.size() == 1
    }


    List<Class> getDomainClasses() {
        return [Org,Location,Nested]
    }


}

@Entity
class Org {
    int id
    String name
    Boolean isActive = false
    BigDecimal amount
    BigDecimal amount2
    Location location
    String secondName
    Date date

    static List quickSearchFields = ["name"]

    static constraints = {
        name blank: true, nullable: true
        isActive nullable: true
        amount nullable: true
        secondName nullable: true
    }
}

@Entity
class Location{
    int id
    String city
    Nested nested
}
@Entity
class Nested{
    String name
    BigDecimal value

    static constraints = {
        name blank: true, nullable: true
        value nullable: true
    }
}

