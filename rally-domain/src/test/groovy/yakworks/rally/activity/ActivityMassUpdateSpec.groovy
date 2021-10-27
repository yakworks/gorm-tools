package yakworks.rally.domain

import yakworks.commons.lang.IsoDateUtil
import gorm.tools.repository.model.RepoEntity
import gorm.tools.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import grails.buildtestdata.TestData
import grails.persistence.Entity
import grails.plugin.viewtools.AppResourceLoader

import org.apache.commons.io.FileUtils
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean

import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

class ActivityMassUpdateSpec extends Specification implements DomainRepoTest<Activity>, SecurityTest  {

    ActivityRepo activityRepo
    AppResourceLoader appResourceLoader

    def setupSpec() {
        defineBeans {
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
            }
            attachmentSupport(AttachmentSupport)
        }
        mockDomains(Customer, Activity, ActivityNote, ActivityLink,
            Org, OrgTag, Payment, AttachmentLink, Attachment, Task, TaskType, TaskStatus
        )
    }

    def "test massupdate - with notes "() {
        setup:
        Org org = Org.create("test", "test", OrgType.Customer).persist()
        Customer customerOne = Customer.create(id: 1, name: "test-1", num: "test-1", org: org).persist()
        Customer customerTwo = Customer.create(id: 2, name: "test-2", num: "test-2", org: org).persist()

        expect:
        Customer.get(1) != null
        Customer.get(2) != null
        activityRepo != null

        when:
        activityRepo.insertMassActivity([customerOne, customerTwo], [summary: 'note_test'])

        then:
        [customerOne, customerTwo].each { id ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Customer', id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.kind == ActKinds.Note
            assert activity.note.body == "note_test"
        }
    }

    def "test massupdate - with new attachments "() {
        setup:
        Org org = Org.create("test", "test", OrgType.Customer).persist()
        Payment paymentOne = Payment.create(id: 1, amount: 100, org: org).persist()
        Payment paymentTwo = Payment.create(id: 2, amount: 200, org: org).persist()


        File origFile = new File(BuildSupport.gradleRootProjectDir, "examples/resources/test.txt")
        byte[] bytes = FileUtils.readFileToByteArray(origFile)
        File tmpFile = appResourceLoader.createTempFile('test.txt', bytes)
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)

        Map changes = [
            summary    : 'attachment_test',
            attachments: [
                [
                    name            : "test.txt",
                    tempFileName    : tempFileName
                ]
            ]
        ]
        expect:
        Payment.get(1) != null
        Payment.get(2) != null
        activityRepo != null

        when:
        activityRepo.insertMassActivity([paymentOne, paymentTwo], changes, null, true)

        then: "Activity with attachments is created for each payments"
        [paymentOne, paymentTwo].each { id ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Payment', id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.summary == "attachment_test"
            Attachment attachment = activity.attachments[0]
            assert attachment
            assert attachment.id
            assert 'test.txt' == attachment.name
        }

        cleanup:
        [paymentOne, paymentTwo].each { id ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Payment', id)
            Activity activity = link?.activity
            Attachment attachment = activity?.attachments[0]
            if (attachment) {
                File dir = appResourceLoader.getLocation("attachments.location")
                File file = new File(dir, attachment.location)
                file.delete()
                file.exists()
            }
        }
    }

    def testMassUpdate_with_task() {
        setup:
        Org org = Org.create("test", "test", OrgType.Customer).persist()
        Customer customerOne = Customer.create(id: 1, name: "test-1", num: "test-1", org: org).persist()
        Customer customerTwo = Customer.create(id: 2, name: "test-2", num: "test-2", org: org).persist()

        TaskType todo = TaskType.build([id:1, name: "TODO"]).persist()
        TaskStatus open = TaskStatus.build([id:0, name: "Open"]).persist()

        expect:
        Customer.get(1) != null
        Customer.get(2) != null

        Map changes = [
            summary: 'task_test',
            task   : [
                dueDate : "2017-04-28",
                priority: 10,
                state   : 1,
                taskType: [id: 1],
                user    : [id: 1, contact: [name: "9ci"]]
            ]
        ]

        List targets = [customerOne, customerTwo]

        when:
        activityRepo.insertMassActivity(targets, changes)

        then: "Activity is created with task for each customer"
        targets.each { Customer it ->
            ActivityLink link = ActivityLink.findByLinkedEntityAndLinkedId('Customer', it.id)
            assert link
            Activity activity = link.activity
            assert activity
            assert activity.summary == "task_test"
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
