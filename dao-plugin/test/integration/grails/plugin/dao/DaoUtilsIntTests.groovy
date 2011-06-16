package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*

class DaoUtilsIntTests extends GroovyTestCase {

	def mocke
	
	protected void setUp() {
		super.setUp()
		mocke = new MockIntDomain(id:100,version:1,name:"Billy")
		mocke.errors = new EmptyErrors("empty") 
	}

	protected void tearDown() {
		super.tearDown()
	}
	
	void testDefaultLocale(){
		def loc = DaoUtils.defaultLocale()
		assert Locale.ENGLISH == loc
	}
	
	void testResolveDomainLabel(){
		//this should have an i18n entry
		//FIXME this used to work when test is run in issolation but not together with other tests
		def lbl = DaoUtils.resolveDomainLabel(new Foo())
		//assertEquals('bar',lbl)
		
		//this doesn't have one
		def lbl2 = DaoUtils.resolveDomainLabel(new MockIntDomain())
		assertEquals('MockIntDomain',lbl2)
		
	}
	
	void testCreateMessage(){
		def msg = DaoUtils.createMessage(mocke)
		assert 'default.created.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testSaveMessage(){
		//test domain without a name field and that has a i18n label
		def msg = DaoUtils.saveMessage(new Foo(id:100,version:1))
		assert 'default.saved.message' == msg.code
		//assert 'bar' == msg.args[0] //FIXME this works when test is run alone but with other tests it doesn't pick up the i18n now
		assert 100 == msg.args[1]
	}
	
	void testSaveFailedMessage(){
		def msg = DaoUtils.saveFailedMessage(mocke)
		assert 'default.not.saved.message' == msg.code
	}
	
	void testUpdateMessage(){
		def msg = DaoUtils.updateMessage(mocke)
		assert 'default.updated.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testUpdateFailMessage(){
		def msg = DaoUtils.updateMessage(mocke,false)
		assert 'default.not.updated.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testDeleteMessage(){
		def ident = DaoUtils.badge(mocke.id,mocke)
		def msg = DaoUtils.deleteMessage(mocke,ident)
		assert 'default.deleted.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}

}

class MockIntDomain{
	Long id
	Long version
	String name
	Errors errors	
}

class Foo{
	Long id
	Long version	
}
