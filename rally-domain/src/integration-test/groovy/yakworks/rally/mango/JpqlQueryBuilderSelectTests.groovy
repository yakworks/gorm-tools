package yakworks.rally.mango

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.jpql.JpqlQueryBuilder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.config.GormConfig
import yakworks.rally.orgs.model.Contact
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class JpqlQueryBuilderSelectTests extends Specification implements DomainIntTest {

    @Autowired
    GormConfig gormConfig

    void buildKitchen(){
        //KitchenSink.withTransaction {
            KitchenSink.repo.createKitchenSinks(10)
       // }
    }

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
    }

    void "Test projections simple no aliasToMap"() {
        given:"Some criteria"
        buildKitchen()
        def criteria = Org.query {
            sum('calc.totalDue')
            groupBy('type')
        }
        criteria.order("calc_totalDue_sum")
        criteria.lt("calc_totalDue_sum", 100.0)

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria) //.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query.trim() == strip('''
            SELECT SUM(org.calc.totalDue) as calc_totalDue_sum, org.type as type
            FROM yakworks.rally.orgs.model.Org AS org
            GROUP BY org.type
            HAVING (SUM(org.calc.totalDue) < :p1)
            ORDER BY calc_totalDue_sum ASC
        ''')
        queryInfo.paramMap == [p1: 100.0]

        when:
        //NOTE: This runs the query as is. Without the .aliasToMap(true) it returns a
        // list of arrays since its not going through the Transformer
        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 3
        //see note above on why its arrays
        res[0][1] == OrgType.Client
        res[0][0] < res[1][0]
        res[1][0] < res[2][0]
    }

    def "sum with QueryArgs"() {
        given:
        def qry = Org.query(
            projections: ['calc.totalDue':'sum', 'type':'group'],
            q: [
                inactive:false,
                'calc_totalDue_sum.$lt':100.0
            ],
            sort:[calc_totalDue_sum:'asc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(qry).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        queryInfo.paramMap == [p1: false, p2: 100]
        query != null
        query.trim() == strip('''
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum, org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE org.inactive=:p1
            GROUP BY org.type
            HAVING (SUM(org.calc.totalDue) < :p2)
            ORDER BY calc_totalDue_sum ASC
        ''')

        when:

        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 3
        res[0]['type'] == OrgType.Client
        res[0]['calc_totalDue_sum'] < res[1]['calc_totalDue_sum']
        res[1]['calc_totalDue_sum'] < res[2]['calc_totalDue_sum']
    }

    def "sum with member and multiples on member"() {
        given:
        def qry = Org.query(
            projections: ['calc.totalDue':'sum', 'type':'group'],
            q: [
                'member.division.id': 6,
                'contact.id': 10
            ]
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(qry).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        queryInfo.paramMap['p1'] == 6
        query != null
        query.trim() == strip('''
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum, org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE org.member.division.id=:p1 AND org.contact.id=:p2
            GROUP BY org.type
        ''')

        // when:
        //
        // List res = Org.executeQuery(query, queryInfo.paramMap)
        //
        // then:
        // res.size() == 4
        // res[0]['type'] == OrgType.Client
        // res[0]['calc_totalDue_sum'] < res[1]['calc_totalDue_sum']
        // res[1]['calc_totalDue_sum'] < res[2]['calc_totalDue_sum']
    }

    def "sum with member and multiples on member.div.num"() {
        given:
        def qry = Org.query(
            projections: ['calc.totalDue': 'sum', 'type': 'group'],
            q: [
                'member.division.num': "6",
                'contact.id'         : 10
            ]
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(qry).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        queryInfo.paramMap['p1'] == "6"
        query != null
        query.trim() == strip('''
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum, org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE org.member.division.num=:p1 AND org.contact.id=:p2
            GROUP BY org.type
        ''')
    }

    //FIXME still need to work out alias
    def "exists on contact location"() {
        given:
        //assert gormConfig.query.dialectFunctions.enabled

        def qryContact = Contact.query(
            q: [
                'location.city': "second City1*",
                'org.id' : ['$eqf': 'org_.id'] //not picking up the org_
            ]
        ).id()

        def qry = Org.query(
            //projections: ['calc.totalDue': 'sum', 'type': 'group'],
            q: [
                'name': "Org1*",
            ]
        ).exists(qryContact)

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(qry as MangoDetachedCriteria)//.aliasToMap(true)
        builder.enableDialectFunctions(true)
        //def builder = JpqlQueryBuilder.of(qryContact as MangoDetachedCriteria)//.aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then: "The query is valid"
        query.trim() == strip('''
            SELECT DISTINCT org FROM yakworks.rally.orgs.model.Org AS org
            WHERE flike(org.name, :p1 ) = true AND
            EXISTS (
            SELECT contact1.id FROM yakworks.rally.orgs.model.Contact contact1
            WHERE flike(contact1.location.city, :p2 ) = true AND contact1.org.id = org.id
            )
        ''')

        when:
        //NOTE: This runs the query as is. Without the .aliasToMap(true) it returns a
        // list of arrays since its not going through the Transformer
        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 11
        //see note above on why its arrays
        // res[0][1] == OrgType.Client
        // res[0][0] < res[1][0]
        // res[1][0] < res[2][0]
    }
}
