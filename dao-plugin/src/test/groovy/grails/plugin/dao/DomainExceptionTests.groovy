package grails.plugin.dao

import org.junit.Test
import org.springframework.validation.Errors
import grails.validation.ValidationException
import grails.test.*

class DomainExceptionTests {

	@Test
	void testSimple() {
		def e = new DomainException("fubar", new EmptyErrors("blah"))
		"validationException" == e.messageMap.code
		def args = []
		args == e.messageMap.args
		"fubar" == e.messageMap.defaultMessage
	}

	@Test
	void testMessageMap() {
		Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
		Map entity = [someEntity: "go cubs"]
		def e = new DomainException(m, entity, new EmptyErrors("blah"))
		"vtest" == e.messageMap.code
		def args = [0]
		args == e.messageMap.args
		"defmsg" == e.messageMap.defaultMessage
		entity == e.entity
	}

	@Test
	void testNoErrors() {
		Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
		Map entity = [someEntity: "go cubs"]
		def e = new DomainException(m, entity)
		"vtest" == e.messageMap.code
		def args = [0]
		args == e.messageMap.args
		"defmsg" == e.messageMap.defaultMessage
		entity == e.entity
	}

}

