package yakworks.rally.orgs


import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
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
        Tag.repo.update(id: 1, entityName: 'Customer')
        Tag.repo.update(id: 2, entityName: 'Customer')
        flushAndClear()
    }

    void "test create orgTag with id"() {
        when:
        setUpData()
        def org = Org.get(10)
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
        def org = Org.get(10)
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
        def org = Org.get(10)
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
        def org = Org.get(10)
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
        def org = Org.get(10)
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
        def org = Org.get(10)
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
        Org from = Org.of("T01", "T01", OrgType.Customer).persist()
        Org to = Org.of("T01", "T01", OrgType.Customer).persist()
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

    void addTagsForSearch(){
        setUpData()
        orgTagRepo.create(10, 1)
        orgTagRepo.create(11, 1)
        orgTagRepo.create(12, 1)
        orgTagRepo.create(12, 2)
        orgTagRepo.create(13, 2)
        orgTagRepo.flushAndClear()
        //sanity check
        assert orgTagRepo.exists(Org.load(10), Tag.load(1))
        assert orgTagRepo.exists(Org.load(11), Tag.load(1))
    }


    void "get all orgs that have ANY tags" () {
        when: "filter where orgs contain ANY of the tags"
        addTagsForSearch()

        String hql = '''\
            select o
            FROM Org as o
            where exists (
                from OrgTag as ot
                where ot.linkedId = o.id
                    and ot.tag.id in (:tagIds)
            )
        '''
        List tag1 = [1L] as List<Long>
        List tag2 = [2L] as List<Long>
        List tagBoth = [1L,2L] as List<Long>

        List hasTag1 = Org.executeQuery(hql, [tagIds:tag1])

        List hasTag2 = Org.executeQuery(hql, [tagIds:tag2])

        List hasBoth = Org.executeQuery(hql, [tagIds:tagBoth])

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        //should return 4
        hasBoth.size() == 4

    }

    void "get all orgs that have ALL of the  tags" () {

        when: "filter where orgs contain ALL of the tags"
        addTagsForSearch()
        String hql = '''\
            select o
            FROM Org as o
            where (
                select count(ot)
                from OrgTag as ot
                where ot.linkedId = o.id
                    and ot.tag.id in (:tagIds)
            ) = :tagIdsSize
        '''
        List tag1 = [1L] as List<Long>
        List tag2 = [2L] as List<Long>
        List tagBoth = [1L,2L] as List<Long>

        List hasTag1 = Org.executeQuery(hql, [tagIds:tag1, tagIdsSize:tag1.size() as Long])

        List hasTag2 = Org.executeQuery(hql, [tagIds:tag2, tagIdsSize:tag2.size() as Long])

        List hasBoth = Org.executeQuery(hql, [tagIds:tagBoth, tagIdsSize:tagBoth.size() as Long])

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        //only id:10 has both
        hasBoth.size() == 1
    }

    void "criteria get all orgs that have ANY tags" () {
        when: "filter where orgs contain ANY of the tags"
        addTagsForSearch()

        def orgCrit = { tagList ->
            DetachedCriteria orgCrit = Org.query {
                exists OrgTag.buildExistsCriteria(tagList)
            }
            return orgCrit
        }

        List hasTag1 = orgCrit([1]).list()
        List hasTag2 = orgCrit([2]).list()
        List has1or2 = orgCrit([1, 2]).list()

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        has1or2.size() == 4

        when: "query by tags"
        List<Org> orgs = Org.query([tags:[[id:1]]]).list()

        then:
        3 == orgs.size()
    }

    void "criteria default org map" () {
        when: "filter where orgs contain ANY of the tags"
        addTagsForSearch()

        def orgCrit = { tagList ->
            return Org.query(tags: tagList)
        }


        List hasTag1 = orgCrit([[id:1]]).list()
        List hasTag2 = orgCrit([[id:2]]).list()
        List has1or2 = orgCrit([[id:1], [id:2]]).list()

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        has1or2.size() == 4

    }

    void "criteria with list of id objects" () {
        when: "filter where orgs contain ANY of the tags"
        addTagsForSearch()

        def orgCrit = { tagList ->
            return Org.query(tags: tagList)
        }


        List hasTag1 = orgCrit([ [id:1] ]).list()
        List hasTag2 = orgCrit([ [id:2] ]).list()
        List has1or2 = orgCrit([ [id:1] , [id:2] ]).list()

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        has1or2.size() == 4

    }

}
