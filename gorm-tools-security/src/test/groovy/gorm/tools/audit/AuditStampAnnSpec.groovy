package gorm.tools.audit

import gorm.tools.security.domain.AppUser
import gorm.tools.utils.GormMetaUtils
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class AuditStampAnnSpec extends Specification implements DomainRepoTest<StampedEntity>, SecurityTest {

    void setupSpec(){
        mockDomains(StampedNoConstraintsClosure)
    }

    void "did it get the audit stamp fields"() {
        when:
        def con = build()
        con.validate()

        // def conProps = StampedEntity.constrainedProperties
        // def conProps = GormMetaUtils.findConstrainedProperties(StampedEntity.getGormPersistentEntity())
        Map conProps = GormMetaUtils.findConstrainedProperties(StampedEntity)

        then:
        ['createdDate','editedDate','createdBy','editedBy'].each{key->
            assert con.hasProperty(key)
        }
        //sanity check the main ones
        conProps.name.nullable == false

        conProps['editedBy'].metaConstraints["bindable"] == false
        conProps['editedBy'].metaConstraints["description"] == "edited by user id"

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert con.hasProperty(it)
            def conProp = conProps[it]
            conProp.metaConstraints["bindable"] == false
            assert !conProp.nullable
            assert !conProp.editable
        }

    }

    void "test when no constraints closure is defined"() {
        when:
        def con = build(StampedNoConstraintsClosure)
        con.validate()

        // def conProps = StampedNoConstraintsClosure.constrainedProperties
        Map conProps = GormMetaUtils.findConstrainedProperties(StampedNoConstraintsClosure)

        then:
        ['createdDate','editedDate','createdBy','editedBy'].each{key->
            assert con.hasProperty(key)
        }

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert con.hasProperty(it)
            def conProp = conProps[it]
            conProp.metaConstraints["bindable"] == false
            assert !conProp.nullable
            assert !conProp.editable
        }

    }

    void test_new_bindable_SanityCheck() {
        // when: "binding occurs"
        // StampedEntity d = new StampedEntity()
        // d.properties = [name: 'test', createdBy:99, editedBy:99]
        //
        // then: "should not have been bound"
        // d.name == 'test'
        // d.createdBy == null
        // d.createdDate == null
        // d.editedBy == null
        // d.editedDate == null

        when: "new binding occurs"
        StampedEntity d2 = new StampedEntity()
        d2.bind([name:'test', createdBy:99, createdDate: new Date(), editedBy:99, editedDate: new Date()])

        then: "should not have been bound"
        d2.name == 'test'
        d2.createdBy == null
        d2.createdDate == null
        d2.editedBy == null
        d2.editedDate == null
    }

    def "test create"() {
        when:
        Long id = StampedEntity.create([name: "Wyatt Oil"]).id
        //flushAndClear()

        then:
        def o = StampedEntity.get(id)
        o.name == "Wyatt Oil"
        o.createdDate
        o.createdBy == 1
        o.editedDate
        o.editedBy == 1
        o.createdDate == o.editedDate
    }

    def "test update"(){
        when:
        Long id = StampedEntity.create([name:"Wyatt Oil"]).id
        flushAndClear()

        then:
        def o = StampedEntity.get(id)
        o.name == "Wyatt Oil"
        o.createdDate
        o.createdBy == 1
        o.editedDate
        o.editedBy == 1
        o.createdDate == o.editedDate
        // o.createdByName == 'admin'
        // o.editedByName == 'admin'

        when: 'its edited then edited should be updated'
        sleep(500)
        o.name = '999'
        o.persist(flush:true)

        then:
        def o2 = StampedEntity.get(id)
        //o.refresh()
        o2.name == '999'
        o2.createdDate < o2.editedDate
        //!DateUtils.isSameInstant(o.createdDate, o.editedDate)
        o.editedBy == 1
    }
}
