package gorm.tools.repository

import gorm.tools.repository.bulk.BulkableRepo
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.grails.web.json.JSONArray
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.testify.model.Address

@Integration
@Rollback
class BulkableRepoSpec extends Specification {

    void "test bulk create"() {
        given:
        List<Map> jsonList = [
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"],
            [street:"Street1", city: "RJT", state: "GJ", zipCode:"1234"]
        ]

        when:
        Job job = ((BulkableRepo)Address.repo).bulkCreate(jsonList)

        then:
        noExceptionThrown()
        job.results != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.results, "UTF-8"))

        then:
        json != null
        json.length() == 3
    }
}
