package gorm.tools.support

import org.grails.testing.GrailsUnitTest

import spock.lang.*

class MessageSourceKeySpec extends Specification implements GrailsUnitTest {

    def setup() {
        messageSource.addMessage("sky.dive", Locale.default, "freefall")
        messageSource.addMessage("sky.dive.with.args", Locale.default, "{0} {1} pull")
        messageSource.addMessage("default.created.message", Locale.default, "{0} {1} created")
    }

    def "test simple getMessage"() {
        when:
        def msgSourceKey = new MsgKey('sky.dive')

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)

        when:'pass args to message that does not have args'
        msgSourceKey = new MsgKey('sky.dive', ['go', 'fast'], null)

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with default"() {
        when:
        def msgSourceKey = new MsgKey('not.there', "def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with args"() {
        when:
        def msgSourceKey = new MsgKey('sky.dive.with.args', ['go', 'fast'], 'foo')

        then:
        'go fast pull' == messageSource.getMessage(msgSourceKey, Locale.default)

        when: "no arg is set"
        msgSourceKey = new MsgKey('sky.dive.with.args')

        then:
        '{0} {1} pull' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with only default message"() {
        when:
        def msgSourceKey = new MsgKey(null, "def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

}
