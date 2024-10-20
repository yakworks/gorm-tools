package yakworks.rally.mango

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Specification
import yakworks.commons.model.SimplePagedList
import yakworks.rally.orgs.model.OrgCalc
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
        def list = qry.list()
        def listJpql = qry.mapList()

        then:
        list.size() == 5
        listJpql.size() == 5
        //same key sets here
        (list[0] as Map).keySet() == ['id', 'type'] as Set
        (listJpql[0] as Map).keySet() == ['id', 'type'] as Set
    }

    def "sum simple mango projection"() {
        when:
        MangoDetachedCriteria qry = Org.query([
            projections: ['calc.totalDue':'sum'],
        ])
        def sumbObj = qry.list()

        then:
        sumbObj.size() == 1
    }

    def "sum projections with type groupBy"() {
        when:
        MangoDetachedCriteria qry = Org.query([
            projections: ['calc.totalDue':'sum', 'type': 'group']
        ])

        List sumbObj = qry.mapList()

        then:
        sumbObj.size() == 5
    }

    def "sum projections with groupBy to test member.division.num"() {
        when:
        //baseline should be same
        // int rowCount = Org.query([
        //     q: [ 'calc.totalDue.$gt': 100.0, 'member.division.id': 6 ]
        // ]).count()
        // assert rowCount == 1

        MangoDetachedCriteria qry = Org.query([
            //num is unique so wont group anything
            projections: ['calc.totalDue':'sum'],
            q: [
                'member.division.id': 6,
                'calc.totalDue.$gt': 100.0
            ]
        ])

        List<Map> sumbObj = qry.mapList(max:10)

        then:
        sumbObj.size() == 1
        sumbObj.totalCount == 1
    }

    void "mapList with pagination"() {
        MangoDetachedCriteria qry = Org.query([projections: ['calc.totalDue':'sum', 'type': 'group']])

        when: "there's 1 record in last page"
        List results = qry.mapList(max:2, offset:4)

        then:
        results
        results.totalCount == 5
        results.size() == 1

        when: "max = 1"
        results = qry.mapList(max:1)

        then:
        results
        results.totalCount == 5
        results.size() == 1
    }

    def "sum projections with type groupBy having on sum"() {
        when:
        MangoDetachedCriteria qry = Org.query([
            projections: ['calc.totalDue':'sum', 'type': 'group'],
            q: [
                'calc_totalDue_sum': ['$gt': 50.0]
            ],
            sort: ['calc_totalDue_sum': 'asc']
        ])

        List sumbObj = qry.mapList()

        then:
        sumbObj.size() == 3
    }

    def "sum projections with type groupBy where on group"() {
        when:
        MangoDetachedCriteria qry = Org.query([
            projections: ['calc.totalDue':'sum', 'type': 'group'],
            q: [
                'type': ['Customer', 'Company']
            ]
        ])

        List sumbObj = qry.mapList()

        then:
        sumbObj.size() == 2
    }

    def "sum projections using mapList same table"() {
        //HERE BE DRAGONS
        when:
        MangoDetachedCriteria qry = OrgCalc.query([
            projections: ['totalDue':'sum'],
            q: [
                'totalDue.$gt': 100
            ]
        ])

        List sumbObj = qry.mapList()

        then:
        sumbObj.size() == 1
        sumbObj[0]['totalDue'] == 12200.0
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
        sumbObj[0]['calc']['totalDue'] < sumbObj[1]['calc']['totalDue']
        sumbObj[1]['calc']['totalDue'] < sumbObj[2]['calc']['totalDue']
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
        sumbObj[0]['calc']['totalDue'] > sumbObj[1]['calc']['totalDue']
        sumbObj[1]['calc']['totalDue'] > sumbObj[2]['calc']['totalDue']
    }

    def "sum with QueryArgs"() {
        when:
        def args = QueryArgs.of().projections('calc.totalDue':'sum', 'type':'group')
        def qry = Org.repo.query(args)
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Customer
    }

    void "test min projection"() {
        setup:
        def query = Org.query {
            createAlias('calc', 'calc')
            createAlias('contact', 'contact')
            projections {
                groupBy("orgTypeId")
                min("calc.totalDue") //this should result in a key calc_totalDue in the result map
            }
        }

        when:
        def results = query.list()

        then:
        results
        results[0] instanceof Map
        ((Map)(results[0])).containsKey("calc_totalDue")
    }


    @Issue("https://github.com/yakworks/gorm-tools/issues/609")
    void "test min projection sep build"() {
        setup:
        def query = Org.query {
            createAlias('calc', 'calc')
            createAlias('contact', 'contact')
            //putting projections here would pass.
        }

        //fails only when min used with groupBy in query.build {}
        query = query.groupBy("orgTypeId").min("calc.totalDue")

        when:
        def results = query.list()
        Map row1 = results[0]

        then:
        results
        row1 instanceof Map
        row1.containsKey("orgTypeId")
        row1.containsKey("calc_totalDue")
    }


    def "sum with projections key as string"() {
        when: 'simulate what comes on url query string'
        def qry = Org.query(projections: "'calc.totalDue':'sum', 'type':'group'", sort:'calc_totalDue:asc')
        def sumbObj = qry.list()

        then:
        //there are 5 types, one for each type
        sumbObj.size() == 5
        sumbObj[0]['type'] == OrgType.Client
        sumbObj[0]['calc_totalDue'] < sumbObj[1]['calc_totalDue']
        sumbObj[1]['calc_totalDue'] < sumbObj[2]['calc_totalDue']
    }

    void "test property projection returns maps"() {
        when:
        def qry = Org.query {
            createAlias('contact', 'contact')
            projections {
                property("contact.id")
                property("contact.name")
            }
            lte("id", 5L)
        }

        def sumbObj = qry.mapList()

        then:
        sumbObj.size() == 5
        sumbObj[0] instanceof Map
    }

    @Ignore("@Joshua shouldnt this work ! createAliases should setup alias automatically ?")
    void "groupBy should auto setup aliases"() {
        when:
        def query = Org.query {
            projections {
                //shouldnt need to explicitely create aliases
                sum('calc.totalDue')
                groupBy("contact.name")
            }
            lte("id", 5)
        }
        def result = query.list()

        then:
        noExceptionThrown()
        result.size() == 5
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

    def "projections with two fields same name should not collide"() {
        when: "two projections have same propname - eg contact.name and org.name"
        def qry = Org.query {
            createAlias('contact', 'contact')
            createAlias('calc', 'calc')
            projections {
                sum('calc.totalDue')
                groupProperty('contact.name')
                groupProperty('name')
            }
        }
        def sumbObj = qry.list()

        then:
        noExceptionThrown()
        sumbObj.size() == 50
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
