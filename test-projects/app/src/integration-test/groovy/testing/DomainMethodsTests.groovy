package testing

import gorm.tools.dao.DaoUtil
import gorm.tools.dao.errors.DomainException
import grails.test.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException
import spock.lang.Specification

//tests the persist and remove methods
@Integration
@Rollback
class DomainMethodsTests extends Specification {
	//static transactional = false
	static dataInit = false

	void setup() {
		//super.setUp()
		//dao = jumperDelegateDao
		//assert dao.domainClass == Jumper
		initData()
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
		assert Jumper.count() == 10
		assert Student.count() == 10
		dataInit = true
	}

	void testPersist(){
		setup:
		initData()
		when:
		def check = Jumper.findByName("jumper1")
		then:
		check.name == "jumper1"
	}

	void testRemove(){
		setup:
		initData()
		when:
		def dom = Student.findByName("student1")
		assert dom.name == "student1"
		dom.remove()
		DaoUtil.flushAndClear()
		then:
		Student.findByName("student1") == null
	}

	void testPersistArgs(){
		when:
		assert new Jumper(name:"jumpargs").persist(flush:true)

		def check = Jumper.findByName("jumpargs")
		then:
		check.name == "jumpargs"
	}

	void testPersistFailValidation(){
		when:
		def jump = new Jumper()
		then:
		try{
			jump.persist()
			fail "it was supposed to fail the save because of validationException"
		}catch(DomainException e){
			e.cause instanceof ValidationException
			e.entity == jump
		}
	}

	void testPersistFailDataAccess(){
		setup:
		initData()
		when:
		def jump = Jumper.first()
		then:
		try{
			Jumper.executeUpdate("update Jumper j set j.version=20 where j.name='jumper1'")
			jump.name='fukt'
			jump.persist(flush:true)
			fail "it was supposed to fail the save because of validationException"
		}catch(DomainException e){
			e.cause instanceof DataAccessException
			//assert e.cause instanceof org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException
			e.entity == jump
			e.messageMap.code == 'default.not.saved.message'
		}
	}

	/*void testRemoveFail(){
		setup:
		initData()
		when:
		def jump = Jumper.findByName("jumper1")
		assert jump
		then:
		try{
			jump.remove()
			fail "it was supposed to fail because of a foreign key constraint"
		}catch(e){
			e.entity == jump
		}
	}*/

	void testInsert(){
		when:
		println "testInsert"
		then:
		try{
			def result = Jumper.insertAndSave([name:"testInsert"])
			DaoUtil.flushAndClear()
			assert result.entity
			"testInsert" == result.entity.name
			// "default.created.message" == result.message.code
			def dom2 = Jumper.findByName("testInsert")
			assert dom2.name == "testInsert"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}

	void testUpdate(){
		when:
		initData()
		def jump = Jumper.findByName("jumper1")
		then:
		assert jump
		try{
			def result = Jumper.update([id:jump.id,name:"testUpdateXXX"])
			DaoUtil.flushAndClear()
			"testUpdateXXX" == result.entity.name
			jump.id == result.entity.id
			//"default.updated.message" == result.message.code
			def dom2 = Jumper.findByName("testUpdateXXX")
			assert dom2.name == "testUpdateXXX"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}

	void testRemoveParams(){
		setup:
		initData()
		when:
		def stud = Student.findByName("student1")
		then:
		try{
			def result = Student.remove([id:stud.id])
			DaoUtil.flushAndClear()
			stud.id ==  result.id
			//"default.deleted.message" == result.message.code
			Student.findByName("student1") == null
		}catch(DomainException e){
			e.printStackTrace()
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}

	void testGetDaoSetup(){
		assertTrue Jumper.dao.class.name.contains("testing.JumperDao")
		println Student.dao.class.name
		// grails.plugin.dao.GormDaoSupport$$EnhancerBySpringCGLIB$$27f04eca
		assert Student.dao.class.name == ("DefaultGormDao")
		assert DropZone.dao.class.name.contains("GormDaoSupport\$\$EnhancerBySpringCGLIB")
		//assertEquals "grails.plugin.dao.GormDaoSupport",DropZone.dao.class.name
	}


}

