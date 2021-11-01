package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class OrgAttachmentLinkTests extends Specification implements DomainIntTest {
    AttachmentRepo attachmentRepo

    void "test copyToOrg"() {
        setup:
        Org from = Org.create("T01", "T01", OrgType.Division).persist()
        Org to = Org.create("T02", "T02", OrgType.Division).persist()

        Attachment attachment =  Attachment.get(1005)
        //assert attachment.location == 'foo'
        //assert attachment.resource.exists()
        Attachment one = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])
        Attachment two = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])

        expect:
        one != null
        two != null

        when:
        AttachmentLink.create(from, one)
        AttachmentLink.create(from, two)
        flush()

        then:
        AttachmentLink.list(from).size() == 2
        AttachmentLink.list(to).size() == 0

        when: "Copy"
        AttachmentLink.repo.copy(from, to)
        flush()

        then:
        AttachmentLink.list(from).size() == 2
        AttachmentLink.list(to).size() == 2
    }
}
