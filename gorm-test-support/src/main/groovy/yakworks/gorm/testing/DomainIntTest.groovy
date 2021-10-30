/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing

import groovy.transform.CompileStatic

import gorm.tools.beans.AppCtx
import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.cache.GrailsCacheAdminService

/**
 * core integration test trait that consolodated the traits
 */
@CompileStatic
trait DomainIntTest implements DataIntegrationTest, SecuritySpecHelper {

    void clearAppConfigCache(){
        getGrailsCacheAdminService().clearCache("appConfig")
    }

    GrailsCacheAdminService getGrailsCacheAdminService(){
        AppCtx.get('grailsCacheAdminService', GrailsCacheAdminService)
    }
}
