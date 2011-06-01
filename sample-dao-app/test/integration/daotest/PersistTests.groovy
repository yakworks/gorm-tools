package daotest

import grails.test.*

class PersistTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testPersistMethod() {
		def p = new Parent()
		p.name = "jim"
		p.child = new Child(name:"bob")
		
		Parent.withTransaction {
			p.persist() //failOnError:true
		}
		def pf = Parent.findByName("jim")
		assert pf.name == "jim"
		
    }
}
