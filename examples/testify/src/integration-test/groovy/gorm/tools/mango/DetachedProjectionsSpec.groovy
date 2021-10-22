package gorm.tools.mango

import java.time.LocalDate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

@Integration
@Rollback
class DetachedProjectionsSpec extends Specification {

    def "sanity Check list"() {
        expect:
        Org.queryList().size() == 20
    }

    def "Filter by Name eq"() {
        when:
        List list = Org.query{
            projections {
                sum('id')
                flex {
                    sum('num1')
                }
            }

            // eq 'name', 'Org23'
        }.list()

        then:
        list.size() == 1
        list[0].name == "Org23"
        list[0].id == 23
    }


}
