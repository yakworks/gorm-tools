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

    void "Test projections simple"() {
        given:"Some criteria"

        def criteria = Org.query {
            sum('calc.totalDue')
            groupBy('type')
        }
        criteria.order("calc_totalDue")
        criteria.lt("calc_totalDue", 100.0)

        when:"A jpa query is built"
        def builder = JpqlQueryBuilder.of(criteria).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        query != null
        query.trim() == strip('''
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue,org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            GROUP BY org.type
            HAVING (SUM(org.calc.totalDue) < :p1)
            ORDER BY calc_totalDue ASC
        ''')
        queryInfo.paramMap == [p1: 100.0]

        when:

        // query = strip("""
        //     SELECT SUM(org.calc.totalDue) as calc_totalDue_sum , org.type as type
        //     FROM yakworks.rally.orgs.model.Org AS org
        //     GROUP BY org.type
        //     HAVING (SUM(org.calc.totalDue) < :p1
        //     ORDER BY calc.totalDue_sum ASC
        // """)
        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 3
        res[0]['type'] == OrgType.Client
        res[0]['calc_totalDue'] < res[1]['calc_totalDue']
        res[1]['calc_totalDue'] < res[2]['calc_totalDue']
    }

    def "sum with QueryArgs"() {
        given:
        def qry = Org.query(
            projections: ['calc.totalDue':'sum', 'type':'group'],
            q: [
                inactive:false,
                'calc_totalDue.$lt':100.0
            ],
            sort:[calc_totalDue:'asc']
        )

        when: "A jpa query is built"
        def builder = JpqlQueryBuilder.of(qry).aliasToMap(true)
        def queryInfo = builder.buildSelect()
        def query = queryInfo.query

        then:"The query is valid"
        queryInfo.paramMap == [p1: false, p2: 100]
        query != null
        query.trim() == strip('''
            SELECT new map( SUM(org.calc.totalDue) as calc_totalDue,org.type as type )
            FROM yakworks.rally.orgs.model.Org AS org
            WHERE (org.inactive=:p1)
            GROUP BY org.type
            HAVING (SUM(org.calc.totalDue) < :p2)
            ORDER BY calc_totalDue ASC
        ''')

        when:

        List res = Org.executeQuery(query, queryInfo.paramMap)

        then:
        res.size() == 4
        res[0]['type'] == OrgType.Client
        res[0]['calc_totalDue'] < res[1]['calc_totalDue']
        res[1]['calc_totalDue'] < res[2]['calc_totalDue']
    }

}
