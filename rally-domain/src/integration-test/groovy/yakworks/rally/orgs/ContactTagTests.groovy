package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.data.DataProblem
import yakworks.problem.data.DataProblemCodes
import yakworks.problem.data.DataProblemException
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.repo.TagLinkRepo

@Integration
@Rollback
class ContactTagTests extends Specification implements DomainIntTest {

    ContactRepo contactRepo
    TagLinkRepo tagLinkRepo

    void addTagsForSearch(){
        def tagMgr = new Tag(id:9, code:'manager', entityName: 'Contact').persist(flush: true)
        def tagCust = new Tag(id:10, code:'cust', entityName: 'Contact').persist(flush: true)

        tagLinkRepo.create(Contact.load(1), tagMgr)
        tagLinkRepo.create(Contact.load(2), tagMgr)
        tagLinkRepo.create(Contact.load(3), tagMgr)
        tagLinkRepo.create(Contact.load(3), tagCust)
        tagLinkRepo.create(Contact.load(4), tagCust)
        flushAndClear()
        //sanity check
        assert tagLinkRepo.exists(Contact.load(1), tagMgr)
        assert tagLinkRepo.exists(Contact.load(4), tagCust)
    }

    void "add tags and make sure filter works"() {
        when:
        addTagsForSearch()
        def contactCrit = { tagList ->
            return Contact.query(tags: tagList)
        }


        List hasTag1 = contactCrit([ [id:9] ]).list()
        List hasTag2 = contactCrit([ [id:10] ]).list()
        List has1or2 = contactCrit([ [id:9] , [id:10] ]).list()

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        has1or2.size() == 4
    }

}
