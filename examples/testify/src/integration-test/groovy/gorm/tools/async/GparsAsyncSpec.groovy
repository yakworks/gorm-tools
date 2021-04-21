package gorm.tools.async

import java.util.concurrent.atomic.AtomicInteger

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testify.model.Project

@Integration
@Rollback
class GparsAsyncSpec extends Specification {

    GparsAsyncSupport asyncSupport

    void "test eachParallel"() {
        given:
        List<Map> list = createList(50)

        expect:
        //starting org count
        Project.count() == 0

        list.size() == 50

        when:
        AtomicInteger count = new AtomicInteger(0)
        asyncSupport.eachParallel(list) { Map item ->
            new Project(num: item.name, name: "name $item.name").persist()
        }

        then:
        Project.count() == 50
    }

    List<Map> createList(int num) {
        List result = []

        for(int i in (1..num)) {
            result << [name:"Record-$i"]
        }

        return result
    }
}
