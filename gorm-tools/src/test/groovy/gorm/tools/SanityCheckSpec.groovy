/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.beans.Pager
import gorm.tools.metamap.services.MetaEntityService
import gorm.tools.metamap.services.MetaMapService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import testing.Cust
import yakworks.i18n.icu.ICUMessageSource
import yakworks.meta.MetaMapList
import yakworks.testing.gorm.GormToolsHibernateSpec

class SanityCheckSpec extends GormToolsHibernateSpec {
    static entityClasses = [Cust]

    @Autowired ApplicationContext applicationCtx
    @Autowired ICUMessageSource msgService
    @Autowired Environment environment

    //something needs to be specifeid

    def "sanity check standard beans"() {
        expect:
        msgService
        environment
        println "Active profiles: ${environment.getActiveProfiles()}"
        println "Grails Env: ${grails.util.Environment.current}"
        applicationCtx
    }

}
