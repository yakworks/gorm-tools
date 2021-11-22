/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem


import spock.lang.Specification
import testing.Cust
import yakworks.i18n.MsgKey

class EntityValidationProblemSpec extends Specification {

    void "simpl string constructor"() {
        when:
        def e = new ValidationProblem("foo message")
        def ve =  e.toException()

        then:
        e.title == ValidationProblem.DEFAULT_TITLE
        ve.message.contains "Validation Error(s): foo message: code=validation.problem"
    }

    void "test cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def e = ValidationProblem.ofCause(rte)
        def ve =  e.toException()

        then:
        e.title == ValidationProblem.DEFAULT_TITLE
        ve.message.contains "Validation Error(s): bad stuff: code=validation.problem"
    }

    void "test msgKey and entity"() {
        when:
        def cust = new Cust()
        def e =  ValidationProblem.ofCode('password.mismatch')
            .detail("The passwords you entered do not match")
            .entity(cust)
        def ve =  e.toException()

        then:
        e.code == 'password.mismatch'
        e.msg.args.asMap() == [name: 'Cust']
        e.title == ValidationProblem.DEFAULT_TITLE
        ve.message.contains("The passwords you entered")
    }

    void "entity and cause"() {
        when:
        def rte = new RuntimeException("bad stuff")
        def cust = new Cust()
        def e =  ValidationProblem.of(cust, rte)
        def ve =  e.toException()

        then:
        e.code == 'validation.problem'
        e.msg.args.asMap() == [name: 'Cust']
        e.title == ValidationProblem.DEFAULT_TITLE
        ve.message == 'Validation Error(s): bad stuff: code=validation.problem'
        ve.rootCause == rte
    }

}
