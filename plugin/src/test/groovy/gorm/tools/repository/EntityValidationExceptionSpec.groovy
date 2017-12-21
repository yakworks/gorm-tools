package gorm.tools.repository

import gorm.tools.repository.errors.EmptyErrors
import gorm.tools.repository.errors.EntityValidationException
import spock.lang.Specification

class EntityValidationExceptionSpec extends Specification {

    void testSimple() {
        when:
        def e = new EntityValidationException("fubar", new EmptyErrors("blah"))

        then:
        "validationException" == e.messageMap.code
        def args = []
        args == e.messageMap.args
        "fubar" == e.messageMap.defaultMessage
    }

    void testMessageMap() {
        setup:
        Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
        Map entity = [someEntity: "go cubs"]

        when:
        def e = new EntityValidationException(m, entity, new EmptyErrors("blah"))

        then:
        "vtest" == e.messageMap.code
        def args = [0]
        args == e.messageMap.args
        "defmsg" == e.messageMap.defaultMessage
        entity == e.entity
    }

    void testNoErrors() {
        setup:
        Map m = [code: "vtest", args: [0], defaultMessage: "defmsg"]
        Map entity = [someEntity: "go cubs"]

        when:
        def e = new EntityValidationException(m, entity)

        then:
        "vtest" == e.messageMap.code
        def args = [0]
        args == e.messageMap.args
        "defmsg" == e.messageMap.defaultMessage
        entity == e.entity
    }

}

