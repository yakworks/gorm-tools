package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.config.OrgProps
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.testing.MockData
import yakworks.security.gorm.model.AppUser
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class TaskSpec extends Specification implements GormHibernateTest, SecurityTest { //implements SecuritySpecUnitTestHelper{
    static List<Class> entityClasses = [
        MailMessage, AttachmentLink, ActivityLink, Activity, Task, TaskType, TaskStatus,
        Org, AppUser, ActivityNote, Contact, ActivityContact, PartitionOrg
    ]
    static List springBeans = [OrgProps]

    @Autowired ActivityRepo activityRepo

    Closure doWithGormBeans() { { ->
        attachmentSupport(AttachmentSupport)
    }}

    void setup(){
        TaskType todo = RepoTestData.build(TaskType, [id:1, name: "Todo"]) //.persist()
        TaskStatus open = RepoTestData.build(TaskStatus, [id:0, name: "Open"]) //.persist()
    }

    Map getActTaskData(Long orgId){
        return [
            org:[id: orgId], //org id does not exist
            name: 'Do Something',
            task: [
                dueDate : "2017-04-28",
                priority: 10,
                state   : 1,
                taskType: [id: 1]
            ]
        ]
    }

    def "create act task"(){
        when:
        def org = MockData.org()
        Map params = getActTaskData(org.id)
        Activity act = Activity.create(params)

        then:
        act
        act.task
        act.name == 'Do Something'
    }

    def "create task with note"(){
        when:
        def org = MockData.org()
        Map params = getActTaskData(org.id)
        params.note = [body: 'test note']
        Activity act = Activity.create(params)

        then:
        act.task
        act.name == 'Do Something'
        act.kind == Activity.Kind.Todo
        act.note.body == 'test note'
    }

}
