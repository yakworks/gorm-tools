package yakworks.rally.activity

import spock.lang.Shared
import spock.lang.Specification
import yakworks.api.problem.data.NotFoundProblem
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.repo.ActivityLinkRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData
import yakworks.rally.seed.RallySeed
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityLinkRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = RallySeed.entityClasses + [Attachment, AttachmentLink, FileData]
    static List springBeans = RallySeed.springBeanList

    @Shared Long orgId

    ActivityLinkRepo getRepo() {
        ActivityLink.repo
    }

    void setupSpec() {
        orgId = MockData.org().id
    }

    Activity createActivity() {
        Activity.create(org: [id: orgId], name: 'shared')
    }

    void "removeLink removes one link and keeps the activity"() {
        setup:
        Activity activity = createActivity()
        repo.create(101L, 'ArTran', activity)
        repo.create(102L, 'ArTran', activity)
        flush()

        when:
        repo.removeLink(activity.id, 'ArTran', 101L)
        flushAndClear()

        then:
        Activity.get(activity.id)
        ActivityLink.findWhere(linkedEntity: 'ArTran', linkedId: 101L) == null
        ActivityLink.findWhere(linkedEntity: 'ArTran', linkedId: 102L).activity.id == activity.id
    }

    void "removeLink deletes the activity when it was the last link"() {
        setup:
        Activity activity = createActivity()
        repo.create(201L, 'ArTran', activity)
        flush()

        when:
        repo.removeLink(activity.id, 'ArTran', 201L)
        flushAndClear()

        then:
        Activity.get(activity.id) == null
        ActivityLink.findWhere(linkedEntity: 'ArTran', linkedId: 201L) == null
    }

    void "remove(activity) deletes links and leaves the activity"() {
        setup:
        Activity activity = createActivity()
        repo.create(301L, 'Payment', activity)
        flush()

        when:
        repo.remove(activity)
        flushAndClear()

        then:
        Activity.get(activity.id)
        ActivityLink.findWhere(linkedEntity: 'Payment', linkedId: 301L) == null
    }
}
