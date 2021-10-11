package gorm.tools.async


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testify.model.Project

@Rollback
@Integration
class ParallelToolsSpec extends Specification {

    ParallelTools parallelTools

    void "test eachParallel"() {
        given:
        List<Map> list = createList(50)

        expect:
        Project.count() == 0
        list.size() == 50

        when:
        //FIXME #339 How is the transactional changes from parallelTools visible to the test.
        def args = new ParallelConfig(enabled: true, transactional: true)
        parallelTools.each(args, list) { Map item ->
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
