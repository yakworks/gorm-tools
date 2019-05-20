package gorm.tools.async

import java.util.concurrent.atomic.AtomicInteger

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import repoapp.Org
import spock.lang.Specification

@Integration
@Rollback
class GparsAsyncSpec extends Specification {

    GparsAsyncSupport asyncSupport

    void "test eachParallel"() {
        given:
        List<Map> list = createList(50)

        expect:
        //starting org count
        Org.count() == 100

        list.size() == 50

        when:
        AtomicInteger count = new AtomicInteger(0)
        asyncSupport.eachParallel(list) { Map item ->
            //Org org = Org.create([name: "name $item.name"])
            new Org([name: "name $item.name"]).save(failOnError:true)
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
