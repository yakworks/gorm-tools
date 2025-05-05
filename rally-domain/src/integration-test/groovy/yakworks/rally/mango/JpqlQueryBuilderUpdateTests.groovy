package yakworks.rally.mango

import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class JpqlQueryBuilderUpdateTests extends Specification implements DomainIntTest {

    String strip(String val){
        val.stripIndent().replace('\n',' ').trim()
    }

    void "simple update query"() {
        given:"Some criteria"

        def criteria = Org.query(
            q: [
                name: "Org1"
                //'contact.firstName': "John1"
            ]
        )

        when:"A jpa query is built"
        JpqlQueryInfo queryInfo = JpqlQueryBuilder.of(criteria)
            .buildUpdate(name: "SinkUp")

        then:"The query is valid"
        queryInfo.query == strip('''\
        UPDATE yakworks.rally.orgs.model.Org org SET org.name=:p1
        WHERE org.name=:p2
        ''')

        // when:
        Org.executeUpdate(queryInfo.query, queryInfo.paramMap)
        Org org = Org.get(1)

        then:
        org.name == "SinkUp"
    }

    void "simple update query with id"() {
        given:"Some criteria"

        def criteria = Org.query(
            q: [
                id: 1
                //'contact.firstName': "John1"
            ]
        )

        when:"A jpa query is built"
        JpqlQueryInfo queryInfo = JpqlQueryBuilder.of(criteria)
            .buildUpdate(name: "SinkUp")

        then:"The query is valid"
        queryInfo.query == strip('''\
        UPDATE yakworks.rally.orgs.model.Org org SET org.name=:p1
        WHERE org.id=:p2
        ''')

        // when:
        Org.executeUpdate(queryInfo.query, queryInfo.paramMap)
        Org org = Org.get(1)

        then:
        org.name == "SinkUp"
    }

}
