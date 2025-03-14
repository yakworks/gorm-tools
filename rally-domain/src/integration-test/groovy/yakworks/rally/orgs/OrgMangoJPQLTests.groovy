package yakworks.rally.orgs

import javax.persistence.criteria.JoinType

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.hibernate.criterion.Projections
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.model.KitchenSink

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip
import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

@Integration
@Rollback
class OrgMangoJPQLTests extends Specification implements DomainIntTest {

    // def "sum totalDu groupby type"() {
    //     when:
    //     List sumbObj = Org.executeQuery('''\
    //         select o.type, sum(o.calc.totalDue)
    //         FROM Org o
    //         GROUP BY o.type
    //     ''')
    //
    //     then:
    //     sumbObj.size() == 5
    //     sumbObj[0]['type'] == OrgType.Customer
    // }
    //(select count(c.id) from Contact c where c.org = o) as contactCount

    @Ignore
    def "sum simple group by"() {
        setup:
        //create some contacts on org1 to test query
        Contact.create( orgId:1, email: "jj@taggart.com", firstName: "Jim")
        Contact.create( orgId:2, email: "bb@taggart.com", firstName: "Joe")
        flush()

        when:
        List sumbObj = Org.executeQuery('''\
            select o, o.formulaz
            FROM Org as o
            Where o.formulaz.contactCount > 2
        ''')

        then:
        sumbObj.size() == 2
        // sumbObj[0]['type'] == OrgType.Customer
    }

    @Ignore
    def "criteria"() {
        when:
        def qry = Org.query {}
        def hibCrit = qry.hibernateQuery.hibernateCriteria
            .setProjection(
                Projections.projectionList().add(Projections.property("o.id"), "id")
            )
        qry = qry.property("contact")
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 10
        // sumbObj[0]['type'] == OrgType.Customer
    }

    @Ignore
    def "hib criteria"() {
        when:
        def qry = Org.query {}
        def hibCrit = qry.hibernateQuery.hibernateCriteria
            .setProjection(Projections.projectionList()
                .add(Projections.property("_Org"), "org")
            )

        def sumbObj = hibCrit.list()

        then:
        sumbObj.size() == 10
        // sumbObj[0]['type'] == OrgType.Customer
    }

    boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

    void "list of properties"() {
        when:"Some criteria"

        MangoDetachedCriteria criteria = Org.query(null)
            .property("id")
            .property("name")
            .join("flex", JoinType.LEFT)

        List listNormal = criteria.list()

        def builder = JpqlQueryBuilder.of(criteria)
        //builder.hibernateCompatible = true
        JpqlQueryInfo queryInfo = builder.buildSelect()

        then:
        listNormal.size() == 50
        compareQuery(queryInfo.query, """
            SELECT org.id as id, org.name as name
            FROM yakworks.rally.orgs.model.Org AS org
            LEFT JOIN org.flex
        """)

    }

}
