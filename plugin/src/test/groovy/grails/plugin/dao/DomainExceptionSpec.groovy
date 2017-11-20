package grails.plugin.dao

import spock.lang.Specification
import grails.test.*

class DomainExceptionSpec extends Specification {

	void testSimple() {
        when:
		def e = new DomainException("fubar", new EmptyErrors("blah"))

        then:
		"validationException" == e.messageMap.code
		def args = []
		args == e.messageMap.args
		"fubar" == e.messageMap.defaultMessage
	}

	void testMessageMap() {
        setup:
		Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
		Map entity = [someEntity: "go cubs"]

        when:
		def e = new DomainException(m, entity, new EmptyErrors("blah"))

        then:
		"vtest" == e.messageMap.code
		def args = [0]
		args == e.messageMap.args
		"defmsg" == e.messageMap.defaultMessage
		entity == e.entity
	}

	void testNoErrors() {
        setup:
		Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
		Map entity = [someEntity: "go cubs"]

        when:
		def e = new DomainException(m, entity)

        then:
		"vtest" == e.messageMap.code
		def args = [0]
		args == e.messageMap.args
		"defmsg" == e.messageMap.defaultMessage
		entity == e.entity
	}

}

