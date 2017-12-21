package gorm.tools.repository

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityValidationException
import grails.persistence.Entity
import grails.testing.gorm.DataTest
import spock.lang.Specification

class RepoUtilsSpec extends Specification implements DataTest {

    void testCheckVersion() {
        when:
        def mocke = new MockDomain([name: "Billy"])
        mocke.version = 1
        mocke.errors = new EmptyErrors("empty")

        then:
        RepoUtil.checkVersion(mocke, 1)

        when:
        RepoUtil.checkVersion(mocke, 0)

        then:
        def e = thrown(EntityValidationException)
        mocke.id == e.entity.id
        "default.optimistic.locking.failure" == e.messageMap.code

    }

    void testCheckFound() {
        try {
            RepoUtil.checkFound(null, [id: '99'], "xxx")
            assert false, "should not have made it here"
        } catch (EntityValidationException e) {
            //id
            assert '99' == e.messageMap.args[1]
            //domain name
            assert 'xxx' == e.messageMap.args[0]
            assert "default.not.found.message" == e.messageMap.code
        }
    }

    void testPropName() {
        def propname = RepoMessage.propName('xxx.yyy.ArDoc')
        assert 'arDoc' == propname
    }

    void testNotFound() {
        def r = RepoMessage.notFound("xxx.MockDomain", [id: "2"])
        assert r.code == "default.not.found.message"
        assert r.args == ["MockDomain", "2"]
        assert r.defaultMessage == "MockDomain not found with id 2"
    }

    void testDefaultLocale() {
        def loc = RepoMessage.defaultLocale()
        assert Locale.ENGLISH == loc
    }

}

@Entity
class MockDomain {
    String name
}
