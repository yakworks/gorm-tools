/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors


import spock.lang.Specification
import testing.Cust
import yakworks.i18n.MsgKey

class EntityValidationExceptionSpec extends Specification {

    void "simpl string constructor"() {
        when:
        def e = new EntityValidationException("foo message")

        then:
        e.title == EntityValidationException.DEFAULT_TITLE
        e.message.contains "Validation Error(s): foo message: code=validation.problem"
    }

    void "test cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def e = new EntityValidationException(rte)

        then:
        e.title == EntityValidationException.DEFAULT_TITLE
        e.message.contains "Validation Error(s): bad stuff: code=validation.problem"
    }

    void "test msgKey and entity"() {
        when:
        def msgKey = MsgKey.of('password.mismatch').fallbackMessage("The passwords you entered do not match")

        def cust = new Cust()
        def e =  EntityValidationException.of(msgKey).entity(cust)

        then:
        e.msg.code == 'password.mismatch'
        e.msg.args == [name: 'Cust']
        e.title == EntityValidationException.DEFAULT_TITLE
        e.message == 'Validation Error(s): code=password.mismatch'
    }

}
