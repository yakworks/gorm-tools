package gorm.tools.repository

import gorm.tools.repository.bulk.BulkableRepo
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONArray
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.testify.model.Address

@Integration
@Rollback
class BulkableRepoIntegrationSpec extends Specification {
    JdbcTemplate jdbcTemplate

    void "test bulk create"() {
        given:
        List<Map> jsonList = [
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street2", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street3", city: "RJT", state: "GJ", zipCode:"1234"]
        ]

        when:
        Job job = ((BulkableRepo)Address.repo).bulkCreate(jsonList)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.data, "UTF-8"))

        then:
        json != null
        json.length() == 3
    }

    @Ignore("Fix XXX in BulkableResults to make this pass")
    void "test data access exception on db constraint violation"() {
        setup:
        jdbcTemplate.execute("CREATE UNIQUE INDEX address_unique ON Address(street,city)")

        and:
        List<Map> jsonList = [
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street2", city: "RJT", state: "GJ", zipCode:"1234"]
        ]

        when:
        Job job = ((BulkableRepo)Address.repo).bulkCreate(jsonList)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.data, "UTF-8"))

        then:
        json != null
        json.length() == 3
        json.ok == true

        cleanup:
        jdbcTemplate.execute("DROP index address_unique")
    }
}
