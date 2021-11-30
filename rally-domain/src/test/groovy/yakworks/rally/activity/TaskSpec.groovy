package yakworks.rally.activity

import gorm.tools.security.domain.AppUser
import gorm.tools.testing.unit.DataRepoTest
import grails.buildtestdata.TestData
import grails.plugin.viewtools.AppResourceLoader
import org.junit.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.SecurityTest
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
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.MockData

class TaskSpec extends Specification implements DataRepoTest, SecurityTest { //implements SecuritySpecUnitTestHelper{
    //Sanity checks and auto runs DomainRepoCrudSpec tests

    void setupSpec(){
        defineBeans{
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
            }
            attachmentSupport(AttachmentSupport)
        }
        mockDomains(AttachmentLink, ActivityLink, Activity, Task, TaskType, TaskStatus,
            Org, AppUser, ActivityNote, Contact, ActivityContact)
    }

    void setup(){
        TaskType todo = TestData.build(TaskType, [id:1, name: "Todo"]) //.persist()
        TaskStatus open = TestData.build(TaskStatus, [id:0, name: "Open"]) //.persist()
    }

    ActivityRepo activityRepo

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

    void "test createTodo"() {
        when:
        def contact = MockData.createContactWithUser()
        Activity activity = activityRepo.createTodo(contact.org, contact.user.id, "Task Summary")

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

    // FIXME https://github.com/yakworks/gorm-tools/issues/410
    //  fails because we look up only by id, code is in AbsractCrossRefRepo.
    //  We can call lookup, but it doesn;t do it automatically
    @Ignore
    def "create task lookup TaskType by code"(){
        when:
        TaskType testType = TestData.build(TaskType, [id:3, name: "test", code: "test"])
        testType.persist()

        assert TaskType.lookup(code: "test")

        def org = MockData.org()
        Map params = getActTaskData(org.id)
        Activity act = Activity.create([taskType: ["code": "test"], org: org, name: 'test me'])

        then:
        act
        act.task
        act.task.code == 'test'
    }
    
}
