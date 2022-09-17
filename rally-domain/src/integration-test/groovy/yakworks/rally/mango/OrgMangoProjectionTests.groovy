package yakworks.rally.mango

import gorm.tools.mango.api.QueryArgs
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
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
        sumbObj[0]['type'] == OrgType.Customer
    }

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

    def "sum and groupby methods order asc"() {
        when:
        def qry = Org.query {}
        qry.sum('calc.totalDue').groupBy('type')
        qry.order('calc_totalDue_sum')
        def sumbObj = qry.mapList()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Client
        sumbObj[0]['calc_totalDue_sum'] < sumbObj[1]['calc_totalDue_sum']
        sumbObj[1]['calc_totalDue_sum'] < sumbObj[2]['calc_totalDue_sum']
    }

    def "sum and groupby methods order desc"() {
        when:
        def qry = Org.query {}
        qry.sum('calc.totalDue').groupBy('type')
        qry.order('calc_totalDue_sum', 'desc')
        def sumbObj = qry.mapList()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Customer
        sumbObj[0]['calc_totalDue_sum'] > sumbObj[1]['calc_totalDue_sum']
        sumbObj[1]['calc_totalDue_sum'] > sumbObj[2]['calc_totalDue_sum']
    }

    def "sum with QueryArgs"() {
        when:
        def args = QueryArgs.withProjections('calc.totalDue':'sum', 'type':'group')
        def qry = Org.query(args)
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Customer
    }

    def "sum with projections key as string"() {
        when: 'simulate what comes on url query string'
        def qry = Org.query(projections: "'calc.totalDue':'sum', 'type':'group'", sort:'calc_totalDue_sum:asc')
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Client
        sumbObj[0]['calc_totalDue_sum'] < sumbObj[1]['calc_totalDue_sum']
        sumbObj[1]['calc_totalDue_sum'] < sumbObj[2]['calc_totalDue_sum']
    }

    def "sum association with closure old school"() {
        when:
        def qry = Org.query {
            createAlias('contact', 'contact')
            createAlias('calc', 'calc')
            projections {
                sum('calc.totalDue')
                groupProperty('contact.name')
                groupProperty('type')
            }
            lte("id", 5)
        }
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 5
        // sumbObj[0][0] == 10
    }

    @Ignore
    def "sum and groupby with some hakery to get having to work"() {
        when:
        def qry = Org.query{
            createAlias('calc', 'calc')
            projections {
                groupProperty('type')
            }
        }

        def sqlProj = Projections.sqlGroupProjection(
            "sum({alias}.totalDue) as calc_totalDue",
            "orgTypeId having calc_totalDue > 100",
            ["calc_totalDue"] as String[],
            [StandardBasicTypes.INTEGER] as Type[])

        // def crit = qry.hibernateQuery.initProjections()
        def crit = qry.hibernateQuery.hibernateCriteria
        def projList = qry.hibernateQuery.hibernateProjections()
        projList.add(Projections.alias(sqlProj, 'calc_totalDue'))
        crit.setProjection(projList)
        crit.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
        def sumbObj = crit.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 2

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
