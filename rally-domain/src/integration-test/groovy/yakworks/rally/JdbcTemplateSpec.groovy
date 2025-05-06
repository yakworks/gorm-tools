package yakworks.rally

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import javax.sql.DataSource

import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgType

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

        def o = new Org(num: '123', name: 'foo', type: OrgType.Customer).persist()
        o.flex = new OrgFlex(date1: ldt)
        o.persist(flush: true)

        then:
        o.flex.date1 == ldt

        when:
        def result = jdbcTemplate.queryForList("select date1 from OrgFlex where id = ${o.id}")

        then:
        result.size() == 1
        result[0].date1 != null

        when:
        //verify that timestamp was stored in UTC. but was retrived in system timzone, so conversion to UTC should match.
        DateFormat fm = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        String td1 = fm.format(result[0].date1)
        fm.setTimeZone(TimeZone.getTimeZone("UTC"))
        String nowString = fm.format(now)

        then:
        nowString == td1
    }

}
