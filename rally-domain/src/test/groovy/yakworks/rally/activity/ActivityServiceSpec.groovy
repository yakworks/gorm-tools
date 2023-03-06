package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.mail.MailMessageSender
import yakworks.rally.mail.config.MailProps
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.mail.testing.TestMailService
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.MockData
import yakworks.security.gorm.model.AppUser
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityServiceSpec extends Specification implements DataRepoTest, SecurityTest { //implements SecuritySpecUnitTestHelper{
    static List<Class> entityClasses = [
        MailMessage, AttachmentLink, ActivityLink, Activity, Task, TaskType, TaskStatus,
        Org, AppUser, ActivityNote, Contact, ActivityContact
    ]

    @Autowired ActivityService activityService

    Closure doWithGormBeans() { { ->
        activityService(ActivityService)
        appResourceLoader(AppResourceLoader)
        attachmentSupport(AttachmentSupport)
        mailMessageSender(MailMessageSender)
        emailService(TestMailService)
        mailConfig(MailProps)
    }}

    @Shared Long orgId

    void setupSpec(){
        // super.setupSpec()
        orgId = MockData.org().id
    }

    void setup(){
        RepoTestData.build(TaskType, [id:1, name: "Todo"])
        RepoTestData.build(TaskStatus, [id:0, name: "Open"])
    }

    void "createLog"() {
        when:
        Activity activity = activityService.createLog(orgId, "got it")

        then:
        activity.name == 'got it'
        activity.kind == Activity.Kind.Log
        activity.orgId == orgId
    }

    void "simple note creation"() {
        when:
        Activity activity = activityService.createNote(orgId, "foo")

        then:
        activity.note != null
        activity.name == 'foo'
        activity.note.body == 'foo'
    }

    // void "logEmail act"() {
    //     when:
    //     def mailMessage = MockData.mailMessage()
    //     assert mailMessage.subject
    //     Activity activity = activityService.logEmail(orgId, mailMessage)
    //
    //     then:
    //     activity.mailMessage
    //     activity.name == mailMessage.subject
    // }

    void "test createTodo"() {
        when:
        def contact = MockData.createContactWithUser()
        Activity activity = activityService.createTodo(contact.org, contact.user.id, "Task Summary")

        then:
        activity != null
        activity.org == contact.org
        activity.name == "Task Summary"
        activity.kind == Activity.Kind.Todo
        activity.task != null
        activity.task.taskType == TaskType.TODO
        activity.task.userId == contact.user.id
        activity.task.dueDate != null
        activity.task.status == TaskStatus.getOPEN()
    }

}
