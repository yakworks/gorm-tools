package yakworks

import org.springframework.beans.factory.annotation.Value

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class ApplicationProperySpec extends Specification {

    @Value('${foo.message:}')
    String message

    @Value('${foo.bar.test-message:}')
    String bazMsg

    void "Check it gets set from the right place"() {
        expect:
        message == "from EXTERNAL examples/resource/foo.yml"
        bazMsg == "got it"
    }

}
