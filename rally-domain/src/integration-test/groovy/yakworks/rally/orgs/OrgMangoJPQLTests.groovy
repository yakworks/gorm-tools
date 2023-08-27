package yakworks.rally.orgs


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.hibernate.criterion.Projections
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org

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

}
