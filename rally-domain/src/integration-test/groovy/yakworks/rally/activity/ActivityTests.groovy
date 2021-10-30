package yakworks.rally.activity

import java.time.LocalDateTime

import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class ActivityTests extends Specification implements DataIntegrationTest {

    void "save activity with an Org that does not exist"(){

        when:
        def params = [
                org:[id: 909090],
                note:[body: 'Todays test note'],
                summary: '!! will get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        flush()

        then:
        act
        act.note.body == params.note.body
        act.summary == params.note.body

        when:
        flushAndClear()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        activity.orgId == params.org.id.toLong()
        activity.org.id == params.org.id.toLong()
        params.note.body == activity.note.body
        params.note.body == activity.summary
        Activity.Kind.Note == activity.kind
        activity.task == null

        when: "Activity is removed, note also gets removed"
        activity.remove()

        then:
        Activity.get(act.id) == null
        ActivityNote.get(activity.noteId) == null
    }

    def testHasAttachments(){
        when:
        def activity = Activity.get(200)//Existing Activity in test d/b without ActivityAttachment

        then:
        activity != null
        !activity.attachments
    }

    def testHasAttachments_Success(){
        when:
        //Adding attachment to existing Activity from test d/b to check for hasAttachments
        def activity = Activity.get(200)
        def att = Attachment.get(1004)
        assert att
        activity.addAttachment(att)
        activity.persist()
        flush()
        then:
        activity.hasAttachments()
    }

    /*
     * Testcase for checking summary with task kind
     */
    def testUpdateSummary_WithTaskKind(){
        when:
        def activity = new Activity()
        activity.title = "test title"
        activity.kind = TaskType.TODO.name
        activity.org = Org.get(2)
        activity.repo.addNote(activity, "test body")
        activity.summary = 'test summary'
        activity.persist()
        flushAndClear()

        then:
        activity.id != null
        //Check summary of saved Activity of kind Task
        activity.title == activity.summary
    }

    def testSaveForContacts() {
        when:
        def activity = new Activity()
        activity.title = "test"
        activity.org = Org.get(2)
        //add activityNote object
        def activityNote =  new ActivityNote()
        activityNote.body = "test note"
        activity.note = activityNote
        activity.summary = 'test summary'

        //add a task
        def task = new Task()
        task.dueDate = LocalDateTime.now()
        task.userId = 50
        task.status = TaskStatus.OPEN
        task.taskType = TaskType.TODO
        activity.task = task

        activity.persist()
        //contact
        ActivityContact.create(activity, Contact.get(50), [flush: true])

        flushAndClear()

        then:
        activity.note?.id != null

        when:
        //find activities for the contact
        def results = ActivityContact.createCriteria().list {
            projections {
                property('activity')
            }
            eq('contact', Contact.get(50))
        }

        then:
        1 == results.size()

        when:
        def activityRes = results[0]

        then:
        activityRes.attachments != null
        0 == activityRes.attachments.size()
        //Checking the contacts assoiciated with activity
        activityRes.contacts != null
        1 == activityRes.contacts.size()

        // cleanup:
        // activityRes.delete(flush:true)
        // flushAndClear()
    }

}
