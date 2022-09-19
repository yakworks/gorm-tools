package yakworks.rally.activity

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class ActivityBulkTests extends Specification implements DomainIntTest {

    ActivityBulk activityBulk

    void "bulk insert note"() {
        setup:
        Org org = Org.of("T01", "T01", OrgType.Customer).persist()
        List<Org> customers = Org.findAllByOrgTypeId(OrgType.Customer.id, [max:5])
        assert customers.size() == 5

        when:
        Activity activity = activityBulk.insertMassNote(customers, "Customer", org, "test note")
        flush()

        activity = Activity.get(activity.id)
        List<ActivityLink> links = ActivityLink.list(activity)

        then:
        links.size() == 5
        activity.note.body == "test note"
        customers.each { Org customer ->
            assert links.find({ it.linkedId == customer.id}) != null
        }

    }

}
