package yakworks

import javax.sql.DataSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class JdbcTemplateSpec extends Specification {

    DataSource dataSource
    JdbcTemplate jdbcTemplate

    void "test jdbc timezone"() {
        expect:
        dataSource != null

        when:
        Date now = new Date()
        LocalDateTime ldt = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault())

        KitchenSink kitchenSink = new KitchenSink(num:'t1', name: "test", actDate: now, localDateTime: ldt)
        kitchenSink.persist(flush: true)

        KitchenSink.repo.flushAndClear()

        kitchenSink = KitchenSink.get(kitchenSink.id)

        then:
        kitchenSink.actDate == now
        kitchenSink.localDateTime == ldt

        when:
        def result = jdbcTemplate.queryForList("select actDate, localDateTime from KITCHENSINK where id = ${kitchenSink.id}")

        then:
        result.size() == 1
        result[0].actDate != null
        result[0].localDateTime != null


        when:
        //verify that timestamp was stored in UTC. but was retrived in system timzone, so conversion to UTC should match.
        DateFormat fm = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        String td1 = fm.format(result[0].actDate)
        String td2 = fm.format(result[0].localDateTime)
        fm.setTimeZone(TimeZone.getTimeZone("UTC"))
        String nowString = fm.format(now)

        then:
        nowString == td1
        td1 == td2
    }

}
