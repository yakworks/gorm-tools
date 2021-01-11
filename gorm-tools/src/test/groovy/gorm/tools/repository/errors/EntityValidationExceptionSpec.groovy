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
        //"validation.error" == e.code
        !e.args
        "fubar" == e.defaultMessage
        e.message.contains('fubar')
    }

    void testNoErrors() {
        setup:
        Map entity = [someEntity: "go cubs"]
        def msgKey = new MsgKey('vtest', 'defmsg')

        when:
        def e = new EntityValidationException(msgKey, entity)

        then:
        "vtest" == e.code
        !e.args
        "defmsg" == e.defaultMessage
        entity == e.entity
    }

}
