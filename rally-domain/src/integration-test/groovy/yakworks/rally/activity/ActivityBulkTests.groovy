package yakworks.rally.activity

import java.nio.file.Path

import org.apache.commons.io.FileUtils

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class ActivityBulkTests extends Specification implements DomainIntTest {

    ActivityBulk activityBulk
    AttachmentSupport attachmentSupport

    void "bulk insert note"() {
        setup:
        Org org = Org.of("T01", "T01", OrgType.Customer).persist()
        List<Org> customers = Org.query(orgTypeId: OrgType.Customer.id).list(max: 5)
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
            assert links.find({ it.linkedId == customer.id }) != null
        }
    }

    void "insertMassActivity note without links"() {
        setup:
        Org org1 = Org.of("AB1", "ActBulk 1", OrgType.Customer).persist()
        Org org2 = Org.of("AB2", "ActBulk 2", OrgType.Customer).persist()
        Contact c1 = Contact.create(firstName: "AB1", org: [id: org1.id])
        Contact c2 = Contact.create(firstName: "AB2", org: [id: org2.id])

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], [name: 'bulk note'])
        flush()

        then:
        activities.size() == 2
        ActivityLink.query(linkedEntity: 'Contact', linkedId: c1.id).count() == 0
        ActivityLink.query(linkedEntity: 'Contact', linkedId: c2.id).count() == 0
        Activity.findWhere(org: org1).note.body == 'bulk note'
        Activity.findWhere(org: org2).note.body == 'bulk note'
    }

    void "insertMassActivity with linkTargets"() {
        setup:
        Org org = Org.of("ABL", "ActBulk Link", OrgType.Customer).persist()
        Contact c1 = Contact.create(firstName: "L1", org: [id: org.id])
        Contact c2 = Contact.create(firstName: "L2", org: [id: org.id])

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], [name: 'linked note'], null, true)
        flush()

        then:
        activities.size() == 1
        Activity.query(org: org).count() == 1
        ActivityLink.findWhere(linkedEntity: 'Contact', linkedId: c1.id).activity == activities[0]
        ActivityLink.findWhere(linkedEntity: 'Contact', linkedId: c2.id).activity == activities[0]
    }

    void "insertMassActivity with attachment"() {
        setup:
        Org org1 = Org.of("ABA1", "ActBulk Att 1", OrgType.Customer).persist()
        Org org2 = Org.of("ABA2", "ActBulk Att 2", OrgType.Customer).persist()
        Contact c1 = Contact.create(firstName: "Att1", org: [id: org1.id])
        Contact c2 = Contact.create(firstName: "Att2", org: [id: org2.id])

        File origFile = new File(BuildSupport.rootProjectDir, "examples/resources/test.txt")
        Path tmpFile = attachmentSupport.createTempFile('test.txt', FileUtils.readFileToByteArray(origFile))

        Map activityData = [
            name: 'note with file',
            attachments: [[name: 'test.txt', tempFileName: tmpFile.fileName.toString()]]
        ]

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], activityData)
        flush()

        then:
        activities.size() == 2
        activities.every { AttachmentLink.list(it).size() == 1 }

        and: "same attachment linked on both activities"
        Long attachmentId = activities[0].attachments[0].id
        activities.every { it.attachments[0].id == attachmentId }
        activities.every { it.attachments[0].name == 'test.txt' }
        activities[0].attachments[0].resource.exists()

        cleanup:
        activities?.each { Activity activity ->
            activity.attachments?.each { it.resource?.file?.delete() }
        }
    }

}
