package grails.plugin.dao

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.validation.Errors
import grails.test.*
import spock.lang.Specification

@Integration
@Rollback
class DaoUtilsIntTests extends Specification {

	def mocke
	
	void setup() {
		//mocke = new MockIntDomain(id:100,version:1,name:"Billy")
		//mocke.errors = new EmptyErrors("empty") 
	}
	
	void testTodo() {
		//todo
		assert true
	}


}

