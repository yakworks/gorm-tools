package grails.plugin.dao

import org.codehaus.groovy.grails.exceptions.GrailsException
import org.springframework.validation.Errors
import grails.validation.ValidationException
import grails.test.*

class GormDataExceptionTests extends GrailsUnitTestCase {

	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testSimple(){
		def e = new GormDataException("fubar",new EmptyErrors("blah"))
		assertEquals "validationException",e.messageMap.code
		def args = []
		assertEquals args,e.messageMap.args
		assertEquals "fubar",e.messageMap.defaultMessage
	}

	void testMessageMap(){
		Map m = [code:"vtest",args:[0],defaultMessage:"defmsg"]
		Map entity = [someEntity:"go cubs"]
		def e = new GormDataException(m,entity,new EmptyErrors("blah"))
		assertEquals "vtest",e.messageMap.code
		def args = [0]
		assertEquals( args, e.messageMap.args)
		assertEquals "defmsg",e.messageMap.defaultMessage
		assertEquals entity,e.entity
	}

	void testNoErrors(){
		Map m = [code:"vtest",args:[0],defaultMessage:"defmsg"]
		Map entity = [someEntity:"go cubs"]
		def e = new GormDataException(m,entity)
		assertEquals "vtest",e.messageMap.code
		def args = [0]
		assertEquals( args, e.messageMap.args)
		assertEquals "defmsg",e.messageMap.defaultMessage
		assertEquals entity,e.entity
	}

}

