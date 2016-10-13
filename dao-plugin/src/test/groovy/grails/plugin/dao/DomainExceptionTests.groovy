package grails.plugin.dao

import org.codehaus.groovy.grails.exceptions.GrailsException
import org.springframework.validation.Errors
import grails.validation.ValidationException
import grails.test.*

class DomainExceptionTests {

	@Test
	void testSimple(){
		def e = new DomainException("fubar",new EmptyErrors("blah"))
		assertEquals "validationException",e.messageMap.code
		def args = []
		assertEquals args,e.messageMap.args
		assertEquals "fubar",e.messageMap.defaultMessage
	}

	@Test
	void testMessageMap(){
		Map m = [code:"vtest",args:[0],defaultMessage:"defmsg"]
		Map entity = [someEntity:"go cubs"]
		def e = new DomainException(m,entity,new EmptyErrors("blah"))
		assertEquals "vtest",e.messageMap.code
		def args = [0]
		assertEquals( args, e.messageMap.args)
		assertEquals "defmsg",e.messageMap.defaultMessage
		assertEquals entity,e.entity
	}

	@Test
	void testNoErrors(){
		Map m = [code:"vtest",args:[0],defaultMessage:"defmsg"]
		Map entity = [someEntity:"go cubs"]
		def e = new DomainException(m,entity)
		assertEquals "vtest",e.messageMap.code
		def args = [0]
		assertEquals( args, e.messageMap.args)
		assertEquals "defmsg",e.messageMap.defaultMessage
		assertEquals entity,e.entity
	}

}

