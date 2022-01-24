package yakworks.rally.orgs

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgTagRepo
import yakworks.rally.tag.model.Tag

@Integration
@Rollback
class OrgTagTests extends Specification implements DomainIntTest {

    OrgTagRepo orgTagRepo

    protected void setUpData() {
        Tag.update(id: 1, entityName: 'Customer')
        Tag.update(id: 2, entityName: 'Customer')
        flushAndClear()
    }

    void "test create orgTag with id"() {
        when:
        setUpData()
        def org = Org.get(100)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        def o = orgTagRepo.create(org, Tag.load(1))
        flushAndClear()
        def orgTag = OrgTag.get(org, Tag.load(1))

        then:
        orgTag
    }

    void "test create orgTag"() {
        when:
        setUpData()
        def org = Org.get(50)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        def o = orgTagRepo.create(org, Tag.get(1))
        flushAndClear()
        def orgTag = OrgTag.get(org, Tag.get(1))

        then:
        orgTag
    }

    void "sanity check methods"() {
        setup:
        setUpData()
        def org = Org.get(50)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        orgTagRepo.create(org, Tag.get(1))
        orgTagRepo.create(org, Tag.get(2))
        flushAndClear()
        def tag1 = Tag.get(1)
        def tag2 = Tag.get(2)

        expect:
        OrgTag.get(org, tag1)
        OrgTag.exists(org, tag1)
        orgTagRepo.exists(org, Tag.get(1))
        orgTagRepo.exists(org, tag1)
        orgTagRepo.exists(org, tag2)
        orgTagRepo.list(org).size() == 2
        orgTagRepo.listTags(org).size() == 2
        //Taggged
        org.tags.size() == 2
        OrgTag.exists(org, tag1)
    }

    void "remove"() {
        when:
        setUpData()
        def org = Org.get(50)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        orgTagRepo.create(org, Tag.get(1))
        flushAndClear()
        assert OrgTag.get(org, Tag.get(1))
        orgTagRepo.remove(org, Tag.get(1))
        flushAndClear()

        then:
        !OrgTag.get(org, Tag.get(1))
    }

    void "remove all"() {
        when:
        setUpData()
        def org = Org.get(50)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        orgTagRepo.create(org, Tag.get(1))
        orgTagRepo.create(org, Tag.get(2))
        flushAndClear()
        assert orgTagRepo.list(org).size() == 2
        orgTagRepo.remove(org) == 2
        flushAndClear()

        then:
        orgTagRepo.list(org).size() == 0
    }

    void "list" () {
        setup:
        setUpData()
        def org = Org.get(50)
        //tag 1 is CPG for Customer and Org:205 is walmart customer
        orgTagRepo.create(org, Tag.load(1))
        orgTagRepo.create(org, Tag.load(2))
        orgTagRepo.flushAndClear()
        orgTagRepo.exists(org, Tag.load(1))

        def orgTagList = orgTagRepo.queryFor(org).list()
        def tagList = orgTagRepo.listTags(org)

        expect:
        orgTagList.size() == 2
        tagList.size() == 2
        tagList[0].name == 'CPG'
        tagList[1].name == 'MFG'
    }

    void "copyToOrg"() {
        setup:
        setUpData()
        Org from = Org.of("T01", "T01", OrgType.Customer.id).persist()
        Org to = Org.of("T01", "T01", OrgType.Customer.id).persist()
        flush()

        orgTagRepo.create(from, Tag.load(1))
        orgTagRepo.create(from, Tag.load(2))

        flush()

        expect:
        from.tags.size() == 2

        when:
        orgTagRepo.copyToOrg(from, to)
        flush()

        then:
        to.tags.size() == 2
        OrgTag.exists(to, Tag.get(1))
        OrgTag.exists(to, Tag.get(2))

    }

}
