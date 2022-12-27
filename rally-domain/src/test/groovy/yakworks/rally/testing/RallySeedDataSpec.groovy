package yakworks.rally.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

@Ignore //see FIXME line 99 in GormHibernateTest
class RallySeedDataSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [
        AppUser, SecRole, SecRoleUser, SecRolePermission,
        Org, OrgSource, OrgTypeSetup, OrgTag, Location, Contact, OrgFlex, OrgCalc, OrgInfo,
        AttachmentLink, ActivityLink, Activity, TaskType,
        Tag, TagLink, Attachment, ActivityNote, Contact, ActivityContact
    ]
    //RallySeedData.entityClasses
    // static springBeans = [
    //     appResourceLoader: AppResourceLoader,
    //     attachmentSupport: AttachmentSupport
    // ]

    @Autowired JdbcTemplate jdbcTemplate

    void setup(){
        assert jdbcTemplate
        RallySeedData.fullMonty(10)
    }

    void "smoke test"() {
        when:
        def o = Org.get(1)

        then:
        o
    }

}
