package gorm.tools.support

import org.grails.testing.GrailsUnitTest

import spock.lang.*

// tests the stock messageSourceKey for spring
class MessageSourceKeySpec extends Specification implements GrailsUnitTest {

    def setup() {
        messageSource.addMessage("sky.dive", Locale.default, "freefall")
        messageSource.addMessage("sky.dive.with.args", Locale.default, "{0} {1} pull")
        messageSource.addMessage("default.created.message", Locale.default, "{0} {1} created")
    }

    def "test simple getMessage"() {
        when:
        def msgSourceKey = SpringMsgKey.of('sky.dive')

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)

        when:'pass args to message that does not have args'
        msgSourceKey = SpringMsgKey.of('sky.dive', ['go', 'fast'])

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with default"() {
        when:
        def msgSourceKey = SpringMsgKey.of('not.there', "def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with args"() {
        when:
        def msgSourceKey = SpringMsgKey.of('sky.dive.with.args', ['go', 'fast'], 'foo')

        then:
        'go fast pull' == messageSource.getMessage(msgSourceKey, Locale.default)

        when: "no arg is set"
        msgSourceKey = SpringMsgKey.of('sky.dive.with.args')

        then:
        '{0} {1} pull' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with only default message"() {
        when:
        def msgSourceKey = SpringMsgKey.ofDefault("def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

}
