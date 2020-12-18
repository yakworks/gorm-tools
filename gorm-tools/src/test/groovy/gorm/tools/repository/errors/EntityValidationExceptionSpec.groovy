/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import gorm.tools.support.MsgKey
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

    void testNoErrors() {
        setup:
        Map entity = [someEntity: "go cubs"]
        def msg = new MsgKey('vtest', 'defmsg')

        when:
        def e = new EntityValidationException(msg, entity)

        then:
        "vtest" == e.messageKey.code
        !e.messageKey.args
        "defmsg" == e.messageKey.defaultMessage
        entity == e.entity
    }

}
