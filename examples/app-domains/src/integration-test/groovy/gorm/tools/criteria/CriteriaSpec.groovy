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
        List list = Contact.createCriteria().list{
            eq "address.city", "City12"
        }
        then:
        list.size() == 1
        list[0].address.dropZone.location == "City12"
    }

    def "test nested withCriteria"() {
        when:
        List list = Org.withCriteria {
            eq "address.dropZone.location", "City22"
        }
        then:
        list.size() == 1
        list[0].address.dropZone.location == "City22"
    }
}
