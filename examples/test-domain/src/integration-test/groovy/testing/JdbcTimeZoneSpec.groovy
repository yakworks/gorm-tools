package testing

import gorm.tools.repository.RepoUtil
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgType

@Integration
@Rollback
class JdbcTimeZoneSpec extends Specification {

    DataSource dataSource
    JdbcTemplate jdbcTemplate

    void "test jdbc timezone"() {
        expect:
        dataSource != null

        when:
        Date now = new Date()
        LocalDateTime ldt = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault())

        Org org = new Org(num:'t1', name: "test", actDate: now, locDateTime: ldt, type: OrgType.Customer)
        org.persist(flush: true)

        RepoUtil.flushAndClear()

        org = Org.get(org.id)

        then:
        org.actDate == now
        org.locDateTime == ldt

        when:
        def result = jdbcTemplate.queryForList("select actDate, locDateTime from ORG where id = ${org.id}")

        then:
        result.size() == 1
        result[0].actDate != null
        result[0].locDateTime != null


        when:
        //verify that timestamp was stored in UTC. but was retrived in system timzone, so conversion to UTC should match.
        DateFormat fm = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        String td1 = fm.format(result[0].actDate)
        String td2 = fm.format(result[0].locDateTime)
        fm.setTimeZone(TimeZone.getTimeZone("UTC"))
        String nowString = fm.format(now)

        then:
        nowString == td1
        td1 == td2
    }

}
