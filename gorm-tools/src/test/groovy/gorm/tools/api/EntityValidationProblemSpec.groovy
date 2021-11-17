/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api


import spock.lang.Specification
import testing.Cust
import yakworks.i18n.MsgKey

class EntityValidationProblemSpec extends Specification {

    void "simpl string constructor"() {
        when:
        def e = new EntityValidationProblem("foo message")

        then:
        e.title == EntityValidationProblem.DEFAULT_TITLE
        e.message.contains "Validation Error(s): foo message: code=validation.problem"
    }

    void "test cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def e = new EntityValidationProblem(rte)

        then:
        e.title == EntityValidationProblem.DEFAULT_TITLE
        e.message.contains "Validation Error(s): bad stuff: code=validation.problem"
    }

    void "test msgKey and entity"() {
        when:
        def msgKey = MsgKey.of('password.mismatch').fallbackMessage("The passwords you entered do not match")

        def cust = new Cust()
        def e =  EntityValidationProblem.of(msgKey).entity(cust)

        then:
        e.msg.code == 'password.mismatch'
        e.msg.args == [name: 'Cust']
        e.title == EntityValidationProblem.DEFAULT_TITLE
        e.message == 'Validation Error(s): code=password.mismatch'
    }

    void "entity and cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def cust = new Cust()
        def e =  EntityValidationProblem.of(cust, rte)

        then:
        e.msg.code == 'validation.problem'
        e.msg.args == [name: 'Cust']
        e.title == EntityValidationProblem.DEFAULT_TITLE
        e.message == 'Validation Error(s): bad stuff: code=validation.problem'
    }

}
