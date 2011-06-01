package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*
import grails.plugin.dao.*
import testing.*

class BasicTestsForDao extends GroovyTestCase {

	def dao
	
/*	protected void setUp() {
		super.setUp()
		dao = jumperDao
		assert dao.domainClass == Jumper
		//pop an item in just to have for things below
	}

	protected void tearDown() {
		super.tearDown()
	}*/

	void testSave(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		try{
			dao.save(dom)
			DaoUtils.flushAndClear()
			def dom2 = Jumper.findByName("testSave")
			assert dom2
		}catch(GormException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testDelete(){
		println "testDelete"
		def dom = new Jumper(name:"testDelete")
		try{
			dao.save(dom)
			DaoUtils.flushAndClear()
			def dom2 = Jumper.findByName("testDelete")
			dao.delete(dom2)
			def dom3 = Jumper.findByName("testDelete")
			assertNull dom3
		}catch(GormException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testInsert(){
		println "testInsert"
		try{
			def result = dao.insert([name:"testInsert"])
			DaoUtils.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals "testInsert", result.entity.name 
			assertEquals "default.created.message", result.message.code
			def dom2 = Jumper.findByName("testInsert")
			assert dom2.name == "testInsert"
		}catch(GormException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testUpdate(){
		println "testUpdate"
		def dup = new Jumper(name:"testUpdate")
		dup.save()
		DaoUtils.flushAndClear()
		assert Jumper.findByName("testUpdate")
		try{
			def result = dao.update([id:dup.id,name:"testUpdateXXX"])
			DaoUtils.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals "testUpdateXXX", result.entity.name 
			assertEquals dup.id, result.entity.id 
			assertEquals "default.updated.message", result.message.code
			def dom2 = Jumper.findByName("testUpdateXXX")
			assert dom2.name == "testUpdateXXX"
		}catch(GormException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testRemove(){
		println "testRemove"
		def dup = new Jumper(name:"testRemove")
		dup.save()
		DaoUtils.flushAndClear()
		assert Jumper.findByName("testRemove")
		try{
			def result = dao.remove([id:dup.id])
			DaoUtils.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals dup.id, result.id
			assertEquals "default.deleted.message", result.message.code
			assertNull Jumper.findByName("testRemove")
		}catch(GormException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}

}

