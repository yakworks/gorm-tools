/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.json.JsonException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.hibernate.QueryException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException

import gorm.tools.beans.Pager
import gorm.tools.hibernate.QueryConfig
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.PathItem
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.security.gorm.UserSecurityConfig
import yakworks.security.user.CurrentUser
import yakworks.spring.AppCtx

/**
 * Helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@Slf4j
@CompileStatic
class DefaultEntityResponderValidator implements EntityResponderValidator {

    @Autowired QueryConfig queryConfig

    QueryArgs validate(QueryArgs qargs) {
        //defaults should be based on queryConfig
    }

}
