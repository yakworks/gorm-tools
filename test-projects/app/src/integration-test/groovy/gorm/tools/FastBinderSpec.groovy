package gorm.tools

import gorm.tools.beans.DateUtil
import gorm.tools.databinding.FastBinder
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import spock.lang.Specification
import daoapp.Org
import daoapp.Address

@Integration
@Rollback
class FastBinderSpec extends Specification {
    FastBinder fastBinder

    void "test bind"() {
        setup:
        Org org = new Org()
        Map params = [name:"test", num: "test", revenue: "100.50"]

        when:
        fastBinder.bind(org, params)

        then:
        org.name == "test"
        org.num == "test"
        org.revenue == 100.50
    }

    void "bind association"() {
        setup:
        Org org = new Org()
        Address address = new Address(city:"test").save(failOnError:true, flush:true)
        Map params = ["address":[id:address.id]]

        when:
        fastBinder.bind(org, params)

        then:
        org.address == address
    }

    void "test bind date"() {
        Org org = new Org()
        Map params = [testDate: "2017-10-10"]

        when:
        fastBinder.bind(org, params)

        then:
        org.testDate != null
        org.testDate == DateUtil.parseJsonDate("2017-10-10")

        when:
        params = [testDate: "2017-10-10T10:10:10"]
        fastBinder.bind(org, params)

        then:
        org.testDate != null
        org.testDate == DateUtil.parseJsonDate("2017-10-10T10:10:10")
    }

    void "test bind boolean"() {
        Org org = new Org()
        Map params = [isActive: "true"]

        when:
        fastBinder.bind(org, params)

        then:
        org.isActive == true

        when:
        params = [isActive: "false"]
        fastBinder.bind(org, params)

        then:
        org.isActive == false

        when:
        params = [isActive: "on"]
        fastBinder.bind(org, params)

        then:
        org.isActive == true
    }
}

