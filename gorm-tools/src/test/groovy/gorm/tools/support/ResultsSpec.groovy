package gorm.tools.support

import gorm.tools.testing.support.GormToolsSpecHelper
import grails.testing.spring.AutowiredTest
import spock.lang.Specification

class ResultsSpec extends Specification implements GormToolsSpecHelper {

    void setupSpec() {
        defineCommonBeans()
        Results.metaClass.getMessage = {
            return MsgService.get(delegate)
        }
    }

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
        MsgService.get( Results.OK.defaultMessage("foo")) == 'foo'
    }

    def "test bulder examples"() {
        when:
        def res = Results.OK('sky.dive.with.args', ['go', 'fast'])
        then:
        'sky.dive.with.args' == res.code
        'go fast pull' == MsgService.get( res )

        when:
        res = Results.OK.msg('flubber')
        then:
        res.ok
        res.message == 'flubber'

        when:
        res = Results.error().msg('flubber')
        then:
        !res.ok
        MsgService.get(res) == 'flubber'
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
        'go fast pull' == MsgService.get( er )
        null == er.defaultMessage
    }

    def "test error with args and def message"() {
        when:
        def er = Results.error('sky.dive.with.args', ['go', 'fast']).defaultMessage('im default')

        then:
        er.code == 'sky.dive.with.args'
        'im default' == er.defaultMessage
    }
}
