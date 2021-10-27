package yakworks.rally.orgs

import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag

class OrgConstraintsSpec extends Specification implements DomainRepoTest<Org>, SecurityTest {
    //Automatically runs the basic crud tests

    def setupSpec(){
        defineBeans{
            //scriptExecutorService(ScriptExecutorService)
            orgDimensionService(OrgDimensionService)
        }
        mockDomains(
            //events need these repos to be setup
            OrgSource, OrgTag, Location, Contact, OrgFlex, OrgCalc, OrgInfo
        )
    }

    //@IgnoreRest
    void "sanity check build"() {
        when:
        def org = build()

        then:
        org.id

    }

    void "did it get the audit stamp fields"() {
        when:
        def org = build()
        def orgProps = Org.constrainedProperties

        then:
        ['createdDate','editedDate','createdBy','editedBy'].each{key->
            assert org.hasProperty(key)
        }
        //sanity check the main ones
        orgProps.name.nullable == false

        orgProps['editedBy'].metaConstraints["bindable"] == false
        orgProps['editedBy'].metaConstraints["description"] == "edited by user id"

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert org.hasProperty(it)
            def conProp = orgProps[it]
            conProp.metaConstraints["bindable"] == false
            assert !conProp.nullable
            assert !conProp.editable
        }

    }

    void "make sure name overrides happened"() {
        when:
        def org = build()
        def orgProps = Org.constrainedProperties

        then:
        orgProps['name'].metaConstraints["description"] == "The full name for this organization"

    }

}
