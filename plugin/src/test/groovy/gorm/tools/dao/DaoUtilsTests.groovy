package gorm.tools.dao

import gorm.tools.dao.errors.DomainException
import gorm.tools.dao.errors.EmptyErrors
import grails.persistence.Entity
import org.junit.Test
import org.springframework.validation.Errors

class DaoUtilsTests {

    @Test
    void testCheckVersion() {
        DaoMessage.metaClass.'static'.resolveMessage = { code, defaultMsg ->
            return defaultMsg
        }
        def mocke = new MockDomain([name: "Billy"])
        mocke.version = 1
        mocke.errors = new EmptyErrors("empty")
        //should pass
        DaoUtil.checkVersion(mocke, 1)
        //shold fail
        try {
            DaoUtil.checkVersion(mocke, 0)
            fail "should not have made it here"
        } catch (DomainException e) {
            assert mocke.id == e.entity.id
            assert "default.optimistic.locking.failure" == e.messageMap.code
        }
    }

    @Test
    void testCheckFound() {
        try {
            DaoUtil.checkFound(null, [id: '99'], "xxx")
            fail "should not have made it here"
        } catch (DomainException e) {
            //id
            assert '99' == e.messageMap.args[1]
            //domain name
            assert 'xxx' == e.messageMap.args[0]
            assert "default.not.found.message" == e.messageMap.code
        }
    }

    @Test
    void testPropName() {
        def propname = DaoMessage.propName('xxx.yyy.ArDoc')
        assert 'arDoc' == propname
    }

    @Test
    void testNotFound() {
        def r = DaoMessage.notFound("xxx.MockDomain", [id: "2"])
        assert r.code == "default.not.found.message"
        assert r.args == ["MockDomain", "2"]
        assert r.defaultMessage == "MockDomain not found with id 2"
    }

//    void testCreateMessage(){
//        def msg = DaoMessage.created(mocke,false)
//        assert 'arDoc' == msg.code
//        assert 'arDoc' == msg.args[0]
//    }

    @Test
    void testDefaultLocale() {
        def loc = DaoMessage.defaultLocale()
        assert Locale.ENGLISH == loc
    }

}

@Entity
class MockDomain {
    String name
}
