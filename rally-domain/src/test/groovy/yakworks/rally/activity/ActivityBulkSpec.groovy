package yakworks.rally.activity

import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.repository.model.RepoEntity
import grails.persistence.Entity
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.OrgCopier
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import java.nio.file.Path

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

/**
 * Unit tests for ActivityBulk.insertMassActivity.
 * Uses slim mock Customer/Payment — only org is needed for grouping/linking.
 */
class ActivityBulkSpec extends Specification implements GormHibernateTest, SecurityTest {

    static List entityClasses = [
        Customer, Payment, Activity, ActivityNote, ActivityLink, Org, OrgTag, Location,
        AttachmentLink, Attachment, FileData, Task, TaskType, TaskStatus, Tag, TagLink
    ]

    static List springBeans = [ActivityBulk, AttachmentSupport, AppResourceLoader, OrgCopier, OrgProps, OrgDimensionService]

    @Autowired ActivityBulk activityBulk
    @Autowired AttachmentSupport attachmentSupport

    def cleanup() {
        attachmentSupport.rimrafAttachmentsDirectory()
    }

    void "empty targets returns empty list"() {
        expect:
        activityBulk.insertMassActivity([], [name: 'x']) == []
        activityBulk.insertMassActivity(null, [name: 'x']) == []
    }

    void "customer activity with note"() {
        setup:
        Org org1 = Org.of("c1", "Cust 1", OrgType.Customer).persist()
        Org org2 = Org.of("c2", "Cust 2", OrgType.Customer).persist()
        Customer c1 = new Customer(num: "c1", name: "Cust 1", org: org1).persist()
        Customer c2 = new Customer(num: "c2", name: "Cust 2", org: org2).persist()

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], [note: [body:'note_test']])
        flush()

        then:
        activities.size() == 2
        ActivityLink.query([:]).count() == 0

        and:
        Activity a1 = Activity.findWhere(org: org1)
        Activity a2 = Activity.findWhere(org: org2)
        a1.kind == ActKinds.Note
        a1.note.body == 'note_test'
        a2.kind == ActKinds.Note
        a2.note.body == 'note_test'
    }

    void "customer activity with task"() {
        setup:
        Org org1 = Org.of("t1", "Task Cust 1", OrgType.Customer).persist()
        Org org2 = Org.of("t2", "Task Cust 2", OrgType.Customer).persist()
        Customer c1 = new Customer(num: "t1", name: "Task Cust 1", org: org1).persist()
        Customer c2 = new Customer(num: "t2", name: "Task Cust 2", org: org2).persist()
        build(TaskType, [id: 1, code: "TODO"]).persist()
        build(TaskStatus, [id: 0, code: "Open"]).persist()

        Map changes = [
            name: 'task_test',
            task: [
                dueDate : "2017-04-28",
                priority: 10,
                state   : 1,
                taskType: [id: 1],
                user    : [id: 1]
            ]
        ]

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], changes)
        flush()

        then:
        activities.size() == 2
        ActivityLink.query([:]).count() == 0
        [org1, org2].each { Org org ->
            Activity activity = Activity.findWhere(org: org)
            assert activity.name == 'task_test'
            assert activity.kind == ActKinds.Todo
            assert activity.task
            assert activity.task.taskType == TaskType.TODO
            assert activity.task.state == Task.State.Complete
            assert activity.task.priority == 10
            assert activity.task.dueDate == IsoDateUtil.parseLocalDateTime("2017-04-28")
        }
    }

    void "payment with linkTargets - one activity per org, ActivityLinks for each payment"() {
        setup:
        Org org = Org.of("pay", "Pay Org", OrgType.Customer).persist()
        Payment p1 = new Payment(amount: 100, org: org).persist()
        Payment p2 = new Payment(amount: 200, org: org).persist()

        when:
        List<Activity> activities = activityBulk.insertMassActivity([p1, p2], [note: [body:'pay_note']], null, true)
        flush()

        then:
        activities.size() == 1
        Activity.query(org: org).count() == 1
        ActivityLink.query([:]).count() == 2

        and:
        Activity activity = activities[0]
        activity.note.body == 'pay_note'
        ActivityLink.findWhere(linkedEntity: 'Payment', linkedId: p1.id).activity == activity
        ActivityLink.findWhere(linkedEntity: 'Payment', linkedId: p2.id).activity == activity
    }

    void "attachments created once and linked to each activity"() {
        setup:
        Org org1 = Org.of("a1", "Att Cust 1", OrgType.Customer).persist()
        Org org2 = Org.of("a2", "Att Cust 2", OrgType.Customer).persist()
        Customer c1 = new Customer(num: "a1", name: "Att Cust 1", org: org1).persist()
        Customer c2 = new Customer(num: "a2", name: "Att Cust 2", org: org2).persist()

        File origFile = new File(BuildSupport.rootProjectDir, "examples/resources/test.txt")
        Path tmpFile = attachmentSupport.createTempFile('test.txt', FileUtils.readFileToByteArray(origFile))

        Map changes = [
            name: 'attachment_test',
            attachments: [[name: 'test.txt', tempFileName: tmpFile.fileName.toString()]]
        ]

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], changes)
        flush()

        then:
        activities.size() == 2
        Attachment.query([:]).count() == 1
        AttachmentLink.query([:]).count() == 2

        and:
        Long attachmentId = Attachment.query([:]).list()[0].id
        activities.each { Activity activity ->
            assert activity.attachments.size() == 1
            assert activity.attachments[0].id == attachmentId
            assert activity.attachments[0].name == 'test.txt'
        }
    }

    void "tags applied to each activity"() {
        setup:
        Org org1 = Org.of("tg1", "Tag Cust 1", OrgType.Customer).persist()
        Org org2 = Org.of("tg2", "Tag Cust 2", OrgType.Customer).persist()
        Customer c1 = new Customer(num: "tg1", name: "Tag Cust 1", org: org1).persist()
        Customer c2 = new Customer(num: "tg2", name: "Tag Cust 2", org: org2).persist()
        Tag tag = Tag.create(name: 'bulk-tag', code: 'bulk-tag', entityName: 'Activity')

        when:
        List<Activity> activities = activityBulk.insertMassActivity([c1, c2], [
            note: [body:'tagged_note'],
            tags: [[id: tag.id]]
        ])
        flush()

        then:
        activities.size() == 2
        activities.each { Activity activity ->
            assert activity.note.body == 'tagged_note'
            assert activity.hasTags()
            assert activity.tags*.id == [tag.id]
        }
    }
}

/** Mock — mass activity only needs org (and id for links). */
@Entity
class Customer implements RepoEntity<Customer> {
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
