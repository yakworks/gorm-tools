package yakworks.rally.orgs

import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.job.DefaultSyncJobService
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.SecurityTest
import yakworks.testing.gorm.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType

class OrgValidationSpec extends Specification implements DomainRepoTest<Org>, SecurityTest {

    Closure doWithDomains() { { ->
        orgDimensionService(OrgDimensionService)
    }}

    void setupSpec() {
        mockDomains(OrgSource, OrgTag, Location, Contact, OrgFlex, OrgCalc, OrgInfo)
    }

    void "sanity check validation"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        def isValid = org.validate()

        then:
        isValid

    }

}
