package grails.plugin.gormtools

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class RepoUtilsIntTests extends Specification {

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

