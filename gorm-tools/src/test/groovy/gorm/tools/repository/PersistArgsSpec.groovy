package gorm.tools.repository


import spock.lang.Specification

class PersistArgsSpec extends Specification {

    void "as Map works"() {
        when:
        def saveArgs = new PersistArgs(
            validate: true,
            insert: true,
            bindId: true
        )
        Map samap = saveArgs as Map

        then: "default should only have failOnError:true"
        samap instanceof Map
        samap == [failOnError: true, validate: true, insert: true, bindId: true]

    }

    void "test AutoClone"() {
        when:
        def saveArgs = new PersistArgs(
            validate: true,
            insert: true,
            bindId: true
        )
        var cloned = saveArgs.clone()

        then:
        cloned == saveArgs
        !cloned.is(saveArgs)
        Map samap = cloned as Map
        samap instanceof Map
        samap == [failOnError: true, validate: true, insert: true, bindId: true]

    }

}
