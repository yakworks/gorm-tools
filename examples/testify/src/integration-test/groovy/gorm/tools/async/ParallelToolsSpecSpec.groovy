package gorm.tools.async


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testify.model.Project

@Rollback
@Integration
class ParallelToolsSpecSpec extends Specification {

    ParallelTools parallelTools

    void "test eachParallel"() {
        given:
        List<Map> list = createList(50)

        expect:
        // Project.withSession {
        //     Project.count() == 50
        // }
        //starting org count
        Project.count() == 0

        list.size() == 50

        when:
        // FIXME #339 how is this working, transaction thats set on the test should not be rolling into parallelTools?
        def args = new ParallelConfig(enabled: true)
        parallelTools.each(args, list) { Map item ->
            new Project(num: item.name, name: "name $item.name").persist()
        }
        // Project.repo.flush()

        then:
        Project.withSession {
            Project.count() == 50
        }

    }

    List<Map> createList(int num) {
        List result = []

        for(int i in (1..num)) {
            result << [name:"Record-$i"]
        }

        return result
    }
}