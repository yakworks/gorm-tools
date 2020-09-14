package gorm.tools.support

import org.grails.testing.GrailsUnitTest

import gorm.tools.repository.RepoMessage
import spock.lang.*
import testing.OrgType

class MessageSourceKeySpec extends Specification implements GrailsUnitTest {

    def setup() {
        messageSource.addMessage("sky.dive", Locale.default, "freefall")
        messageSource.addMessage("sky.dive.with.args", Locale.default, "{0} {1} pull")
        messageSource.addMessage("default.created.message", Locale.default, "{0} {1} created")
    }

    def "test simple getMessage"() {
        when:
        def msgSourceKey = new MessageSourceKey('sky.dive')

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)

        when:'pass args to message that does not have args'
        msgSourceKey = new MessageSourceKey('sky.dive', ['go', 'fast'], null)

        then:
        'freefall' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with default"() {
        when:
        def msgSourceKey = new MessageSourceKey('not.there', "def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with args"() {
        when:
        def msgSourceKey = new MessageSourceKey('sky.dive.with.args', ['go', 'fast'], 'foo')

        then:
        'go fast pull' == messageSource.getMessage(msgSourceKey, Locale.default)

        when: "no arg is set"
        msgSourceKey = new MessageSourceKey('sky.dive.with.args')

        then:
        '{0} {1} pull' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with only default message"() {
        when:
        def msgSourceKey = new MessageSourceKey(null, "def msg")

        then:
        'def msg' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

    def "test with RepoMessage"() {
        when:
        Map msgMap = RepoMessage.created(new OrgType(id:1, name: "Biz"))
        //has default.created.message
        def msgSourceKey = new MessageSourceKey(msgMap)

        then:
        'OrgType Biz created' == messageSource.getMessage(msgSourceKey, Locale.default)
    }

}
