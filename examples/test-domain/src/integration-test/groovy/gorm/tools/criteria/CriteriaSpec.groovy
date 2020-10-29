package gorm.tools.criteria

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.taskify.domain.Contact
import yakworks.taskify.domain.Org

@Integration
@Rollback
class CriteriaSpec extends Specification {

    def "test createCriteria for nested property"() {
        when:
        List list = Org.createCriteria().list{
            eq "location.city", "City12"
        }
        then:
        list.size() == 1
    }

    def "test nested query"() {
        when:
        List list = Org.query {
            eq "location.city", "City22"
        }.list()

        then:
        list.size() == 1
    }
}
