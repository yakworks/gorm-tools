package grails.plugin.dao

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import spock.lang.Specification

@Integration
@Rollback
class DaoMessageTests extends Specification {

	def mocke
	
	protected void setup() {

		mocke = new MockIntDomain(id:100,version:1,name:"Billy")
		mocke.errors = new EmptyErrors("empty") 
	}

	void testDefaultLocale(){
		def loc = DaoMessage.defaultLocale()
		assert Locale.ENGLISH == loc
	}
	
	void testResolveDomainLabel(){
		//this should have an i18n entry
		//FIXME this used to work when test is run in issolation but not together with other tests
		def lbl = DaoMessage.resolveDomainLabel(new Foo())
		//assertEquals('bar',lbl)
		
		//this doesn't have one
		def lbl2 = DaoMessage.resolveDomainLabel(new MockIntDomain())
		assertEquals('MockIntDomain',lbl2)
		
	}
	
	void testCreateMessage(){
		def msg = DaoMessage.created(mocke)
		assert 'default.created.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testSaveMessage(){
		//test domain without a name field and that has a i18n label
		def msg = DaoMessage.saved(new Foo(id:100,version:1))
		assert 'default.saved.message' == msg.code
		//assert 'bar' == msg.args[0] //FIXME this works when test is run alone but with other tests it doesn't pick up the i18n now
		assert 100 == msg.args[1]
	}
	
	void testSaveFailedMessage(){
		def msg = DaoMessage.notSaved(mocke)
		assert 'default.not.saved.message' == msg.code
	}
	
	void testUpdateMessage(){
		def msg = DaoMessage.updated(mocke)
		assert 'default.updated.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testUpdateFailMessage(){
		def msg = DaoMessage.notUpdated(mocke)
		assert 'default.not.updated.message' == msg.code
		assert 'MockIntDomain' == msg.args[0]
		assert 'Billy' == msg.args[1]
	}
	
	void testDeleteMessage(){
		def ident = DaoMessage.badge(mocke.id,mocke)
		def msg = DaoMessage.deleted(mocke,ident)
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
