package yakworks.security.audit

import gorm.tools.utils.GormMetaUtils
import spock.lang.Specification
import yakworks.security.PasswordConfig
import yakworks.security.gorm.model.AppUser
import yakworks.security.services.PasswordValidator
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class AuditStampAnnSpec extends Specification implements GormHibernateTest, SecurityTest {

    static List entityClasses = [StampedEntity, StampedNoConstraintsClosure, AppUser]
    static List springBeans = [PasswordConfig, PasswordValidator]
    
    void "did it get the audit stamp fields"() {
        when:
        def con = build(StampedEntity)
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
        new AppUser(id:1, name: "admin", username:"admin", email:"admin@9ci.com", password: "test").persist(flush: true)
        flushAndClear()

        then:
        AppUser.get(1)
        def o = StampedEntity.get(id)
        o.name == "Wyatt Oil"
        o.createdDate
        o.createdBy == 1
        o.editedDate
        o.editedBy == 1
        o.createdDate == o.editedDate
        o.createdByUser.username == 'admin'
        o.editedByUser.username == 'admin'
        o.createdByUser.displayName == 'admin'
        o.editedByUser.displayName == 'admin'
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
