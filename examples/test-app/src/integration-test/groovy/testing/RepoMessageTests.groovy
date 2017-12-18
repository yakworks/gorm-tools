package grails.plugin.gormtools

import gorm.tools.repository.RepoMessage
import gorm.tools.repository.errors.EmptyErrors
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.validation.Errors
import spock.lang.Specification

@Integration
@Rollback
class RepoMessageTests extends Specification {

    def mocke

    protected void setup() {

        mocke = new MockIntDomain(id: 100, version: 1, name: "Billy")
        mocke.errors = new EmptyErrors("empty")
    }

    void testDefaultLocale() {
        def loc = RepoMessage.defaultLocale()
        assert Locale.ENGLISH == loc
    }

    void testResolveDomainLabel() {
        //this should have an i18n entry
        //FIXME this used to work when test is run in issolation but not together with other tests
        def lbl = RepoMessage.resolveDomainLabel(new Foo())
        //assertEquals('bar',lbl)

        //this doesn't have one
        def lbl2 = RepoMessage.resolveDomainLabel(new MockIntDomain())
        assertEquals('MockIntDomain', lbl2)

    }

    void testCreateMessage() {
        def msg = RepoMessage.created(mocke)
        assert 'default.created.message' == msg.code
        assert 'MockIntDomain' == msg.args[0]
        assert 'Billy' == msg.args[1]
    }

    void testSaveMessage() {
        //test domain without a name field and that has a i18n label
        def msg = RepoMessage.saved(new Foo(id: 100, version: 1))
        assert 'default.saved.message' == msg.code
        //assert 'bar' == msg.args[0] //FIXME this works when test is run alone but with other tests it doesn't pick up the i18n now
        assert 100 == msg.args[1]
    }

    void testSaveFailedMessage() {
        def msg = RepoMessage.notSaved(mocke)
        assert 'default.not.saved.message' == msg.code
    }

    void testUpdateMessage() {
        def msg = RepoMessage.updated(mocke)
        assert 'default.updated.message' == msg.code
        assert 'MockIntDomain' == msg.args[0]
        assert 'Billy' == msg.args[1]
    }

    void testUpdateFailMessage() {
        def msg = RepoMessage.notUpdated(mocke)
        assert 'default.not.updated.message' == msg.code
        assert 'MockIntDomain' == msg.args[0]
        assert 'Billy' == msg.args[1]
    }

    void testDeleteMessage() {
        def ident = RepoMessage.badge(mocke.id, mocke)
        def msg = RepoMessage.deleted(mocke, ident)
        assert 'default.deleted.message' == msg.code
        assert 'MockIntDomain' == msg.args[0]
        assert 'Billy' == msg.args[1]
    }

    void testNotFound() {
        when: "Id is null"
        Map params = [id: null]
        Map m = RepoMessage.notFound("Test", params)

        then:
        noExceptionThrown()
        m.code == "default.not.found.message"
        m.defaultMessage == "Test not found with id null"

        when: "Id is not null"
        m = RepoMessage.notFound("Test", [id: 1])

        then:
        noExceptionThrown()
        m.code == "default.not.found.message"
        m.defaultMessage == "Test not found with id 1"

    }

}

class MockIntDomain {
    Long id
    Long version
    String name
    Errors errors
}

class Foo {
    Long id
    Long version
}
