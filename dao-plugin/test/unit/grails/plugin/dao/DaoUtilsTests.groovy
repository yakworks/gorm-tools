package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*

class DaoUtilsTests extends GrailsUnitTestCase {

	def mocke
	
	protected void setUp() {
		super.setUp()
		mocke = new MockDomain(id:100,version:1,name:"Billy")
		mocke.errors = new EmptyErrors("empty") 
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testCheckVersion(){
		DaoUtils.metaClass.'static'.resolveMessage = {code,defaultMsg ->
			return defaultMsg
		}
		//should pass
		DaoUtils.checkVersion(mocke,1)
		//shold fail
		try{
			DaoUtils.checkVersion(mocke,0)
			fail "should not have made it here"
		}catch(DomainException e){
			assertEquals(mocke.id,e.entity.id)
			//println e.entity.errors
			assertEquals("default.optimistic.locking.failure",e.messageMap.code)
		}
	}

	void testCheckFound(){
		try{
			DaoUtils.checkFound(null, [id:'99'],"xxx")
			fail "should not have made it here"
		}catch(DomainException e){
			//id
			assertEquals('99',e.messageMap.args[1])
			//domain name
			assertEquals('xxx',e.messageMap.args[0])
			assertEquals("default.not.found.message",e.messageMap.code)
		}
	}
	
	void testPropName(){
		def propname = DaoUtils.propName('xxx.yyy.ArDoc')
		assert 'arDoc' == propname
	}
	
	void testNotFound(){
		def r = DaoUtils.notFoundResult("xxx.MockDomain",[id:"2"])
		assertTrue r.code == "default.not.found.message"
		assertTrue r.args == ["MockDomain","2"]
		assertTrue r.defaultMessage == "MockDomain not found with id 2"
	}
	
/*	void testCreateMessage(){
		def msg = DaoUtils.createMessage(mocke,false)
		assert 'arDoc' == msg.code
		assert 'arDoc' == msg.args[0]
	}*/
	
	void testDefaultLocale(){
		def loc = DaoUtils.defaultLocale()
		assert Locale.ENGLISH == loc
	}

}

class MockDomain{
	Long id
	Long version
	String name
	Errors errors	
}
