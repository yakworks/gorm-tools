package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class QueryGotchaSpec extends Specification implements DomainIntTest {

    void "session cache and query gotchas"() {
        setup:
        Long kid = 1L
        KitchenSink sink = KitchenSink.get(kid) //this puts instance in session cache

        expect:
        sink.inactive == false //initial value

        when:
        KitchenSink.executeUpdate("update KitchenSink set inactive=true Where id = $kid") //updated to true through hql

        then:
        KitchenSink.findById(kid).inactive == false //doesnt see change
        KitchenSink.findWhere(id: kid).inactive == false //doesnt see changes

        and:
        KitchenSink.findWhere(id: kid, inactive: true) //sees the change ! finds records based on id & inactive=true

        and:
        KitchenSink.findWhere(id: kid, inactive: true).inactive == false //Strange, could find where inactive=true, but field value is false

        and:
        KitchenSink.query(id: kid, inactive: true).get().inactive == false //doesnt see the change here either, but notice, it could find based on "isReconciled:true"

        and:
        KitchenSink.findAllWhere(id: kid)[0].inactive == false //doesnt see change
        KitchenSink.query(id: kid).list()[0].inactive == false //doesnt see change

        and:
        KitchenSink.executeQuery("select new map(k.inactive as inactive) from KitchenSink k where k.id = $kid")[0].inactive == true //sees the change
        jdbcTemplate.queryForObject("select inactive from KitchenSink where id = $kid", Boolean) == true //sees change

        when:
        flush()

        then:
        KitchenSink.findWhere(id: kid).inactive == false //doesnt see change after flush

        when:
        clear()

        then:
        KitchenSink.findWhere(id: kid).inactive == true //sees the change after clear
    }

    void "all good when instance not in session cache"() {
        when:
        KitchenSink.executeUpdate("update KitchenSink set inactive=true Where id = 1")

        then:
        KitchenSink.findById(1L).inactive == true
        KitchenSink.findWhere(id: 1L).inactive == true
    }

    void "hql update does not increment version"() {
        setup:
        int oldVersion = KitchenSink.get(1L).version

        when:
        KitchenSink.executeUpdate("update KitchenSink set inactive=true Where id = 1")
        flushAndClear()

        KitchenSink k = KitchenSink.get(1L)

        then:
        k.inactive == true //sees the change

        and: "version dint update"
        k.version == oldVersion
    }
}
