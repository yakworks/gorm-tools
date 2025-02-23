package yakworks.rally.orgs

import gorm.tools.utils.GormMetaUtils
import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.seed.RallySeed
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class OrgConstraintsSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = RallySeed.entityClasses
    static List springBeans = RallySeed.springBeanList

    void "sanity check build"() {
        when:
        def org = build(Org)

        then:
        org.id
    }

    void "did it get the audit stamp fields"() {
        when:
        def org = build(Org)

        Map oProps = GormMetaUtils.findConstrainedProperties(Org)

        then:
        ['createdDate','editedDate','createdBy','editedBy'].each{key->
            assert org.hasProperty(key)
        }
        //sanity check the main ones
        oProps.name.nullable == false

        oProps['editedBy'].metaConstraints["bindable"] == false
        oProps['editedBy'].metaConstraints["description"] == "edited by user id"

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert org.hasProperty(it)
            def conProp = oProps[it]
            conProp.metaConstraints["bindable"] == false
            assert !conProp.nullable
            assert !conProp.editable
        }

    }

    void "make sure name overrides happened"() {
        when:
        def org = build(Org)
        Map orgProps = GormMetaUtils.findConstrainedProperties(Org)

        then:
        orgProps['name'].metaConstraints["description"] == "The full name for this organization"

    }

}
