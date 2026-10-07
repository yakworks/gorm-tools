package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.orgs.model.Org
import yakworks.rally.seed.RallySeed
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityQuerySpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = RallySeed.entityClasses + [Task, TaskStatus]
    static List springBeans = RallySeed.springBeanList + [ActivityService]

    @Autowired ActivityService activityService

    void setup() {
        new TaskType(id: 1L, name: "Todo", kind: Activity.Kind.Todo, code: "todo").persist()
        new TaskStatus(id: 0L, name: "Open", state: Task.State.Open, code: "open").persist()
        new TaskStatus(id: 1L, name: "Complete", state: Task.State.Complete, code: "complete").persist()
        flushAndClear()
    }

    Map seedActivities() {
        def contact = MockData.createContactWithUser()
        Org org = contact.org
        Activity note = activityService.createNote(org.id, "a note")
        Activity log = activityService.createLog(org.id, "a log")
        Activity openTodo = activityService.createTodo(org, contact.user.id, "open todo")
        Activity completedTodo = activityService.createTodo(org, contact.user.id, "done todo")
        activityService.completeTask(completedTodo.task, contact.user.id)
        completedTodo.persist()
        flushAndClear()
        assert note.task == null
        assert log.task == null
        assert openTodo.task.state == Task.State.Open
        assert completedTodo.task.state == Task.State.Complete
        return [
            orgId: org.id,
            noteId: note.id,
            logId: log.id,
            openTodoId: openTodo.id,
            completedTodoId: completedTodo.id
        ]
    }

    Set alwaysVisible(Map seed) {
        [seed.noteId, seed.logId, seed.openTodoId] as Set
    }

    void "filter by task.state"() {
        given:
        Map seed = seedActivities()

        when: "completed restriction: no task OR task.state = 0 (ActivityQuery left-joins Task)"
        List list = Activity.query([
            q: [
                orgId: seed.orgId,
                $or: [
                    [task: '$isNull'],
                    ['task.state': 0]
                ]
            ]
        ]).list()

        then: "notes, logs and open todos — not dropped by the task.state filter"
        list*.id as Set == alwaysVisible(seed)
        !list*.id.contains(seed.completedTodoId)
    }

    void "completed restriction still returns notes, tasks and others"() {
        given:
        Map seed = seedActivities()

        when: "default filter: no task OR task.state = 0 (ActivityQuery left-joins task)"
        List withRestriction = Activity.query([
            q: [
                orgId: seed.orgId,
                $or: [
                    [task: '$isNull'],
                    ['task.state': 0]
                ]
            ]
        ]).list()

        then: "notes, other task-less kinds, and open todos"
        withRestriction*.id as Set == alwaysVisible(seed)
        !withRestriction*.id.contains(seed.completedTodoId)

        when: "no completed restriction"
        List withoutRestriction = Activity.query([q: [orgId: seed.orgId]]).list()

        then: "same notes, others and tasks, plus the completed todo"
        withoutRestriction*.id as Set == (alwaysVisible(seed) + seed.completedTodoId)
    }

}
