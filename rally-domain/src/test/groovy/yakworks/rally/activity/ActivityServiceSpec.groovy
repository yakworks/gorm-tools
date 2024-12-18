package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.seed.RallySeed
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityServiceSpec extends Specification implements GormHibernateTest, SecurityTest { //implements SecuritySpecUnitTestHelper{
    static List entityClasses = RallySeed.entityClasses
    static List springBeans = RallySeed.springBeanList + [ActivityService]

    @Autowired ActivityService activityService

    @Shared Long orgId

    void setupSpec(){
        // super.setupSpec()
        orgId = MockData.org().id
    }

    void setup(){
        TaskType todo = new TaskType(id:1L, name: "Todo", kind:Activity.Kind.Todo, code:"test").persist()
        TaskStatus status = new TaskStatus(id:0L, name: "Open", state:Task.State.Open, code:"test").persist()

        flushAndClear()

        assert todo.id == 1L
        assert status.id == 0L
        assert TaskType.TODO
        assert TaskStatus.OPEN
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
        expect:
        TaskType.TODO != null

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
