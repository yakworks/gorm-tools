/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.hibernate.QueryConfig
import gorm.tools.mango.api.QueryArgs


/**
 * Helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@Slf4j
@CompileStatic
class DefaultEntityResponderValidator implements EntityResponderValidator {

    @Autowired QueryConfig queryConfig
    @Autowired UserSecurityConfig userSecurityConfig

    QueryArgs validate(QueryArgs qargs) {
        //defaults should be based on queryConfig
    }

}
