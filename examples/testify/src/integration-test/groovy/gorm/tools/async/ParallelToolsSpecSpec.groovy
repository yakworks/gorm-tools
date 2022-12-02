package gorm.tools.async

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Rollback
@Integration
class ParallelToolsSpecSpec extends Specification {

    ParallelTools parallelTools

    void "test eachParallel"() {
        given:
        List<Map> list = createList(50)

        expect:
        //starting org count
        Org.count() == 100
        list.size() == 50

        when:
        // FIXME #339 how is this working, transaction thats set on the test should not be rolling into parallelTools?
        AsyncArgs args = new AsyncArgs(enabled: true)
        parallelTools.each(args, list) { Map item ->
            new Org(num: item.name, name: "name $item.name", type: OrgType.Customer).persist()
        }

        then:
        Org.count() == 150
    }

    List<Map> createList(int num) {
        List result = []

        for(int i in (1..num)) {
            result << [name:"Record-$i"]
        }

        return result
    }
}
