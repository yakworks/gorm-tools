package yakworks.rally.mango


import gorm.tools.mango.jpql.JpqlQueryBuilder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class JpqlQueryBuilderSelectTests extends Specification implements DomainIntTest {

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
    }

    void "Test projections simple no aliasToMap"() {
        given:"Some criteria"

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
            SELECT SUM(org.calc.totalDue) as calc_totalDue_sum,org.type as type
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
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum,org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE (org.inactive=:p1)
            GROUP BY org.type
            HAVING (SUM(org.calc.totalDue) < :p2)
            ORDER BY calc_totalDue_sum ASC
        ''')

        when:

        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 4
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
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum,org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE (org.member.division.id=:p1 AND org.contact.id=:p2)
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
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue_sum,org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE (org.member.division.num=:p1 AND org.contact.id=:p2)
            GROUP BY org.type
        ''')
    }
}
