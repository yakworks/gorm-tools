/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.testing

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.validation.ValidationCode
import spock.lang.Specification
import yakworks.i18n.icu.GrailsICUMessageSource
import yakworks.rally.listeners.RallyEventListener
import yakworks.rally.mail.MailTo
import yakworks.rally.mail.config.MailConfig
import yakworks.testing.gorm.model.ValidationEntity
import yakworks.testing.gorm.unit.GormHibernateTest

class TestMailServiceSpec extends Specification implements GormHibernateTest  {
    static springBeans = [testMailService: TestMailService, mailConfig: MailConfig]
    //static List entityClasses = [ValidationEntity]

    @Autowired TestMailService testMailService
    // Closure doWithGormBeans() { { ->
    //     testMailService(TestMailService)
    // }}

    void "smoke test"() {
        when:
        def res = testMailService.send(new MailTo(to: ["foo@bar.com"]))

        then:
        res.ok
        testMailService.sentMail.size() == 1
    }

    void "test failure"() {
        when:
        def res = testMailService.send("bad", new MailTo(to: ["foo@bar.com"]))
        def res2 = testMailService.send(new MailTo(to: ["bad@email.com"]))

        then:
        !res.ok
        !res2.ok
        //testMailService.sentMail.size() == 1
    }


}
