package yakworks.rally.orgs

import java.time.LocalDateTime

import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.tag.model.TagLink
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.repo.TagLinkRepo

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip


@Integration
@Rollback
class ContactTagTests extends Specification implements DomainIntTest {

    TagLinkRepo tagLinkRepo

    boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

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

    void "test the NOT restriction"() {
        when:
        addTagsForSearch()

        List hasTag1 = Contact.query([
            '$not':[
                [tags: [ [id:9], [id:10]  ]]
                //[name: 'foo']
                //['$exists': TagLink.repo.buildExistsCriteria([9], Contact, 'contact_.id')]
            ]
        ]).list()

        then:
        hasTag1.size() == 96

    }

    void "test the NOT closure"() {
        when:
        addTagsForSearch()

        List hasTag1 = Contact.query {
            not {
                //there are 4 orgs with these tags
                exists TagLink.repo.buildExistsCriteria([9,10], Contact, 'contact_.id')
                //filter out org40-org49, so  10 more orgs
                ilike('name', 'foo%')
            }
        }.list()

        then:
        hasTag1.size() == 96

    }

    void "test Jpql with simple not"() {
        when:
        addTagsForSearch()

        def criteria = Contact.query([
            '$not':[
                id: 1, name: 'bill'
            ]
        ])

        def qlist = criteria.list()

        def builder = JpqlQueryBuilder.of(criteria)
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then: "The query is valid"
        qlist.size() == 99
        compareQuery(queryInfo.where, """
            NOT (contact.id=:p1 OR contact.name=:p2)
        """)

    }

    void "test Jpql"() {
        when:
        addTagsForSearch()

        String date = LocalDateTime.now().plusDays(1).format('yyyy-MM-dd')

        def criteria = Contact.query([
            name: ['$like': "John4%"],
            '$not':[
                ['org.name': ['$like': "%PUBLIX%"] ],
                [tags: [ [id:9], [id:10] ]],
                //[name: 'foo']
                //['$exists': TagLink.repo.buildExistsCriteria([9,10], Contact, 'contact_.id')]
            ],
            "editedDate": ['$lte': "2040-09-16"]
        ])

        def qlist = criteria.list()

        def builder = JpqlQueryBuilder.of(criteria)
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then: "The query is valid"
        qlist.size() == 10
        // queryInfo.query == strip("""
        //     SELECT DISTINCT arTran FROM nine.ar.tran.model.ArTran AS arTran
        //     WHERE (arTran.refnum=:p1)
        // """)
        compareQuery(queryInfo.where, """
            contact.name like :p1
            AND NOT (contact.org.name like :p2
            OR EXISTS ( SELECT DISTINCT tagLink2 FROM yakworks.rally.tag.model.TagLink tagLink2
            WHERE tagLink2.linkedId = contact.id AND tagLink2.linkedEntity=:p3 AND tagLink2.tag.id IN (:p4,:p5) ) )
            AND contact.editedDate <= :p6
        """)

        queryInfo.paramMap == [
            p1: 'John4%',
            p2: '%PUBLIX%',
            p3: 'Contact',
            p4: 9,
            p5: 10,
            p6: LocalDateTime.parse('2040-09-16T00:00')
        ]

        when:
        //NOTE: This runs the query as is. Without the .aliasToMap(true) it returns a
        // list of arrays since its not going through the Transformer
        List res = Contact.executeQuery(queryInfo.query, queryInfo.paramMap)

        then:
        res.size() == 10
    }

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
    }
}
