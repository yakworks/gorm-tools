package yakworks.taskify.domain

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.IgnoreRest
import spock.lang.Specification
import gorm.tools.security.testing.SecuritySpecHelper

@Integration
@Rollback
class OrgCrudSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    def "test Org create"(){
        when:
        println "test Org create"
        Long id = Org.create([num:'123', name:"Wyatt Oil"]).id
        flushAndClear()

        then:
        def c = Org.get(id)
        c.num == '123'
        c.name == 'Wyatt Oil'
        c.createdDate
        c.createdBy == 1
        c.editedDate
        c.editedBy == 1

    }

    def "test Org create fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", type: [id: 1], link: ["name": "", num:'foo2', type: [id: 1]]]
        def org = new Org()

        org.bind(invalidData2)
        org.validate()
        //Org.create(invalidData2)

        then:
        //def ex = thrown(EntityValidationException)
        org.errors.errorCount == 3
        org.errors['kind'].code == 'nullable'
        org.errors['link.kind'].code == 'nullable'
        org.errors['link.name'].code == 'nullable'
    }

    void "org and orgext validation success"(){
        when:
        def org = new Org(num:'foo1', name: "foo", type: OrgType.get(1), kind: Org.Kind.CLIENT)
        org.ext = new OrgExt(org: org, textMax: 'fo')
        org.persist()

        then:
        org.id
        org.ext.id
    }

    void "rejectValue only in LocationRepo beforeValidate"(){
        when:
        def org = new Org(num:'foo1', name: "foo", type: OrgType.get(1), kind: Org.Kind.CLIENT)
        org.location = new Location(city: "LocationRepoVille")
        org.persist(flush: true)
        //flushAndClear()

        then:
        def ex = thrown(EntityValidationException)
        ex.errors.errorCount == 1
        org.location.errors['city'].code == 'from.LocationRepo'
        //TODO need somethign to make this work
        // org.errors.errorCount == 1
    }

    void "org and orgext rejectValue in beforeValidate"(){
        when:
        def org = new Org(num:'foo1', name: "foo", type: OrgType.get(1), kind: Org.Kind.CLIENT)
        org.location = new Location(city: "LocationRepoVille", country: 'USA', street: 'OrgRepoStreet')
        org.ext = new OrgExt(org: org, textMax: 'foo') //foo is 3 chars and should fail validation
        org.persist()
        //flushAndClear()

        then:
        def ex = thrown(EntityValidationException)
        //normal validation errors
        org.errors['ext.textMax'].code == 'maxSize.exceeded'
        org.errors['location.country'].code == 'maxSize.exceeded'
        //since its in orgRepo beforeValidate it shows up as nested
        org.errors['location.street'].code == 'from.OrgRepo'
        //error is on the association for rejects in beforeValidation
        org.location.errors['city'].code == 'from.LocationRepo'
    }

    def "test Org create validation fail"(){
        when:
        Map invalidData2 = [num:'foo1', name: "foo", type: [id: 1], link: ["name": "", num:'foo2', type: [id: 1]]]
        Org.create(invalidData2)

        then:
        def ex = thrown(EntityValidationException)
        //its only 2 on this one as a default kind is set in the repo during create
        ex.errors.errorCount == 2
        ex.errors['link.kind']
        ex.errors['link.name'].code == 'nullable'
    }

    def "test NameNum constraints got added"(){
        when:
        def conProps = Org.constrainedProperties

        then:
        conProps.num.property.nullable == false
        conProps['num'].property.blank == false
        conProps['num'].property.maxSize == 50
        conProps['name'].property.nullable == false
    }
}
