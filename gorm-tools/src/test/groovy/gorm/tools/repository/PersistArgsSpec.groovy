package gorm.tools.repository


import spock.lang.Specification

class PersistArgsSpec extends Specification {

    void "as Map works"() {
        when:
        def saveArgs = new PersistArgs()
        Map samap = saveArgs as Map

        then: "default should only have failOnError:true"
        samap instanceof Map
        samap == [failOnError: true]

    }

}
