package yakworks.rally.domain

import java.nio.file.Path

import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.repository.model.RepoEntity
import grails.persistence.Entity
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.ActivityBulk
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.OrgCopier
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

@Ignore //XXX this whole thing is flaky
class ActivityBulkSpec extends Specification implements GormHibernateTest, SecurityTest  {
    static List entityClasses = [
        Customer, Activity, ActivityNote, ActivityLink, Org, OrgTag, Location, Payment,
        AttachmentLink, Attachment, Task, TaskType, TaskStatus
    ]
    static List springBeans = [ActivityBulk, AttachmentSupport, OrgCopier, OrgProps, OrgDimensionService ]

    @Autowired ActivityBulk activityBulk
    @Autowired AttachmentSupport attachmentSupport

    def "test massupdate - with notes "() {
        setup:
        Org org = new Org(id:1, num: "test", name: "test", type: OrgType.Customer, companyId: 2)
        Org org2 = new Org(id:2, num: "test2", name: "test2", type: OrgType.Customer, companyId: 2)
        Customer customerOne = Customer.repo.create([id: 1, name: "test-1", num: "test-1", org: org],[bindId: true]).persist()
        Customer customerTwo = Customer.repo.create([id: 2, name: "test-2", num: "test-2", org: org2],[bindId: true]).persist()

        expect:
        Customer.get(1) != null
        Customer.get(2) != null

        when:
        activityBulk.insertMassActivity([customerOne, customerTwo], [name: 'note_test'])

        then:
        [customerOne, customerTwo].each { customer ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Customer', customer.id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.kind == ActKinds.Note
            assert activity.note.body == "note_test"
        }
    }

    @Ignore //XTEST flaky test
    def "test massupdate - with new attachments "() {
        setup:
        Org org = Org.of("test", "test", OrgType.Customer).persist()
        Payment p1 = Payment.create(amount: 100, org: org).persist()
        Payment p2 = Payment.create(amount: 200, org: org).persist()


        File origFile = new File(BuildSupport.rootProjectDir, "examples/resources/test.txt")
        byte[] bytes = FileUtils.readFileToByteArray(origFile)
        Path tmpFile = attachmentSupport.createTempFile('test.txt', bytes)
        String tempFileName = tmpFile.fileName

        Map changes = [
            name: 'attachment_test',
            attachments: [ [name: "test.txt", tempFileName: tempFileName] ]
        ]
        expect:
        Payment.get(p1.id) != null
        Payment.get(p2.id) != null

        when:
        activityBulk.insertMassActivity([p1, p2], changes, null, true)

        then: "Activity with attachments is created for each payments"
        [p1, p2].each { id ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Payment', id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.name == "attachment_test"
            Attachment attachment = activity.attachments[0]
            assert attachment
            assert attachment.id
            assert 'test.txt' == attachment.name
        }

        cleanup:
        [p1, p2].each { id ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Payment', id)
            Activity activity = link?.activity
            Attachment attachment = activity?.attachments[0]
            if (attachment) {
                attachment.resource.file.delete()
            }
        }
    }

    def testMassUpdate_with_task() {
        setup:
        Org org = Org.of("test", "test", OrgType.Customer).persist()
        Customer c1 = Customer.create(name: "test-1", num: "test-1", org: org).persist()
        Customer c2 = Customer.create(name: "test-2", num: "test-2", org: org).persist()

        TaskType todo = build(TaskType, [id:1, code: "TODO"]).persist()
        TaskStatus open = build(TaskStatus, [id:0, code: "Open"]).persist()

        expect:
        Customer.get(1) != null
        Customer.get(2) != null

        Map changes = [
            name: 'task_test',
            task: [
                dueDate : "2017-04-28",
                priority: 10,
                state   : 1,
                taskType: [id: 1],
                user    : [id: 1, contact: [name: "9ci"]]
            ]
        ]

        List targets = [c1, c2]

        when:
        activityBulk.insertMassActivity(targets, changes)

        then: "Activity is created with task for each customer"
        targets.each { Customer it ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Customer', it.id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.name == "task_test"
            assert activity.kind == ActKinds.Todo
            assert activity.task
            assert activity.task.taskType == TaskType.TODO
            assert activity.task.state == Task.State.Complete
            assert activity.task.priority == 10
            assert activity.task.dueDate == IsoDateUtil.parseLocalDateTime("2017-04-28")
        }
    }
}

//Just mock the domains for test, we dont need any fields in this domains other then org, because mass udpate uses
// the domains just to get the orgid and doesnt do anything else with these domains
@Entity
class Customer implements RepoEntity<Customer>{
    String num
    String name

    static belongsTo = [org: Org]

    static mapping = {
        id generator: 'foreign', params: [property: 'org']
        org insertable: false, updateable: false, column: 'id'
    }
}

@Entity
class Payment implements RepoEntity<Payment> {
    BigDecimal amount
    static belongsTo = [org: Org]
}
