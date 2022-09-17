/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import spock.lang.Specification
import testing.Cust
import yakworks.i18n.icu.ICUMessageSource
import yakworks.testing.gorm.unit.GormHibernateTest

class SanityCheckSpec extends Specification implements GormHibernateTest {
    static entityClasses = [Cust]

    @Autowired ApplicationContext applicationCtx
    @Autowired Environment environment

    //something needs to be specifeid

    def "sanity check standard beans"() {
        expect:
        messageSource
        environment
        println "Active profiles: ${environment.getActiveProfiles()}"
        println "Grails Env: ${grails.util.Environment.current}"
        applicationCtx
    }

}
