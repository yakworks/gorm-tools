package gorm.tools.support

import org.grails.testing.GrailsUnitTest

import spock.lang.Specification

class ResultsSpec extends Specification implements GrailsUnitTest {

    def setup() {
        messageSource.addMessage("sky.dive", Locale.default, "freefall")
        messageSource.addMessage("sky.dive.with.args", Locale.default, "{0} {1} pull")
        messageSource.addMessage("default.created.message", Locale.default, "{0} {1} created")
    }

    def "test errors"() {
        expect:
        Results.error().ok == false
        Results.OK().ok == true
        Results.OK.ok == true
        Results.OK.code('foo').code == 'foo'
        Results.OK.id(123).id == 123
        Results.OK.message("foo").message == 'foo'
    }

    def "test bulder examples"() {
        when:
        def res = Results.OK('sky.dive.with.args', ['go', 'fast'])
        then:
        'sky.dive.with.args' == res.code
        'go fast pull' == res.message

        when:
        res = Results.OK.message('flubber')
        then:
        res.ok
        res.message == 'flubber'

        when:
        res = Results.error.message('flubber')
        then:
        !res.ok
        res.message == 'flubber'
    }

    def "test error"() {
        when:
        def er = Results.error('sky.dive')

        then:
        er.code == 'sky.dive'
        'freefall' == er.message
    }

    def "test error with args"() {
        when:
        def er = Results.error('sky.dive.with.args', ['go', 'fast'])

        then:
        er.code == 'sky.dive.with.args'
        'go fast pull' == er.message
        null == er.defaultMessage
    }

    def "test error with args and def message"() {
        when:
        def er = Results.error('sky.dive.with.args', ['go', 'fast'], 'im default')

        then:
        er.code == 'sky.dive.with.args'
        'go fast pull' == er.message
        'im default' == er.defaultMessage
    }
}
