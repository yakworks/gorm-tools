package grails.plugin.dao

import org.springframework.validation.Errors
import grails.test.*
import grails.plugin.dao.*
import testing.*
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.junit.*


//tests the persist and remove methods
class DomainMethodsTests extends GroovyTestCase //FIXME extends BasicTestsForDao 
{
	//static transactional = false
	static dataInit = false

	void setUp() {
		//super.setUp()
		//dao = jumperDelegateDao
		//assert dao.domainClass == Jumper
		if(!dataInit) initData()
	}
	
	void initData(){
		println "testSave"
		(1..10).each{
			def jumper= new Jumper(name:"jumper$it")
			//jumper.student = new Student(name:"student$it")
			assert jumper.persist()
			def stud = new Student(name:"student$it")
			stud.jumper = jumper
			assert stud.persist()
		}
		//assert dom.persist()
		DaoUtil.flushAndClear()
		assert Jumper.count() == 10
		assert Student.count() == 10
		dataInit = true
	}
	
	void testPersist(){
		def check = Jumper.findByName("jumper1")
		assert check.name == "jumper1"
	}
	
	void testRemove(){
		def dom = Student.findByName("student1")
		assert dom.name == "student1"
		dom.remove()
		DaoUtil.flushAndClear()
		assertNull( Student.findByName("student1") )
	}
	
	void testPersistArgs(){
		assert new Jumper(name:"jumpargs").persist(flush:true)

		def check = Jumper.findByName("jumpargs")
		assert check.name == "jumpargs"
	}
	
	void testPersistFailValidation(){
		def jump = new Jumper()
		try{
			jump.persist()
			fail "it was supposed to fail the save because of validationException"
		}catch(DomainException e){
			assert e.cause instanceof ValidationException
			assert e.entity == jump
		}
	}
	
	void testPersistFailDataAccess(){
		def jump = Jumper.findByName("jumper1")
		
		try{
			Jumper.executeUpdate("update Jumper j set j.version=20 where j.name='jumper1'")
			DaoUtil.flush()
			jump.name='fukt'
			jump.persist(flush:true)
			fail "it was supposed to fail the save because of validationException"
		}catch(DomainException e){
			assert e.cause instanceof DataAccessException
			assert e.cause instanceof org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
			assert e.entity == jump
			assert e.messageMap.code == 'default.not.saved.message'
		}
	}
	
	void testRemoveFail(){
		def jump = Jumper.findByName("jumper1")
		assert jump
		try{
			jump.remove()
			fail "it was supposed to fail because of a foreign key constraint"
		}catch(DomainException e){
			assert e.cause instanceof DataIntegrityViolationException
			assert e.entity == jump
		}
	}
	
	void testInsert(){
		println "testInsert"
		try{
			def result = Jumper.insert([name:"testInsert"])
			DaoUtil.flushAndClear()
			assert result.entity 
			assertEquals "testInsert", result.entity.name 
			assertEquals "default.created.message", result.message.code
			def dom2 = Jumper.findByName("testInsert")
			assert dom2.name == "testInsert"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testUpdate(){
		def jump = Jumper.findByName("jumper1")
		assert jump
		try{
			def result = Jumper.update([id:jump.id,name:"testUpdateXXX"])
			DaoUtil.flushAndClear()
			assertEquals "testUpdateXXX", result.entity.name 
			assertEquals jump.id, result.entity.id 
			assertEquals "default.updated.message", result.message.code
			def dom2 = Jumper.findByName("testUpdateXXX")
			assert dom2.name == "testUpdateXXX"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testRemoveParams(){
		def stud = Student.findByName("student1")
		try{
			def result = Student.remove([id:stud.id])
			DaoUtil.flushAndClear()
			assertEquals stud.id, result.id
			assertEquals "default.deleted.message", result.message.code
			assertNull Student.findByName("student1")
		}catch(DomainException e){
			e.printStackTrace()
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testGetDaoSetup(){
		assertTrue Jumper.dao.class.name.contains("testing.JumperDao")
		println Student.dao.class.name
		// grails.plugin.dao.GormDaoSupport$$EnhancerBySpringCGLIB$$27f04eca
		assertTrue Student.dao.class.name.contains("GormDaoSupport\$\$EnhancerBySpringCGLIB")
		assertEquals "grails.plugin.dao.GormDaoSupport",DropZone.dao.class.name
	}


}

