package yakworks.rally.orgs


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryAliasAwareSession
import org.grails.orm.hibernate.HibernateSession
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class OrgMangoProjectionTests extends Specification implements DomainIntTest {

    def "sum simple"() {
        when:
        def qry = Org.query {
            projections {
                sum('id')
                groupProperty('type')
            }
        }
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 5
        sumbObj[0][1] == OrgType.Customer
    }

    // start of how to do sums on deep associations
    def "sum association sum method"() {
        when:
        def qry = Org.query {
            lte("id", 5)
        }
        qry.sum('calc.totalDue')
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 1
        sumbObj[0] == 150
    }

    // start of how to do sums on deep associations
    def "sum with group"() {
        when:
        def qry = Org.query {}
        qry.sum('calc.totalDue')
        qry.groupBy('type')
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
    }

    // start of how to do sums on deep associations
    @IgnoreRest
    def "sum with projection map"() {
        when:
        def qry = Org.query {}
        qry.sum('calc.totalDue')
        qry.groupBy('type')
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
    }

    // start of how to do sums on deep associations
    def "sum association"() {
        when:
        def qry = Org.query {
            createAlias('contact', 'contact')
            createAlias('calc', 'calc')
            // sum('calc.totalDue')
            // group('type')
            // projections {
            // sum('calc.totalDue')
            // groupProperty('contact.name')
            // groupProperty('type')
            // }
            lte("id", 5)

        }
        qry.sum('calc.totalDue')
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 5
        // sumbObj[0][0] == 10
    }

    //https://stackoverflow.com/questions/21898926/using-sum-and-arithmetic-result-as-order-key-in-hibernate-criteria
    /*
    Criteria criteria = hibernateSession
                .createCriteria(YourEntity.class);
        criteria.setProjection(Projections
                .projectionList()
                .add(Projections.property("anId").as("prop1"))
                .add(Projections.sum("fieldA").as("prop2"))
                .add(Projections.sum("fieldB").as("prop3"))
                .add(Projections.sqlProjection(
                        "sum(fieldA) + sum(fieldB) as total",
                        new String[] { "total" },
                        new Type[] { StandardBasicTypes.INTEGER }), "total")
                .add(Projections.groupProperty("remarks")));
        criteria.addOrder(Order.desc("total"));
        criteria.setMaxResults(5);
        criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List list = criteria.list();
        for (Object object : list) {
            Map<Object, Object> map = (Map<Object, Object>) object;
            System.out.println(map);
        }
     */
}
