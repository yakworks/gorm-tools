/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.beans.AppCtx
import gorm.tools.beans.map.MetaMapIncludes
import grails.plugin.cache.Cacheable

/**
 * Service to generate the MetaMapSchema from a MetaMapIncludes thats cached.
 * Leans on the build in MetaMapSchema but wrapped in sevice here so it can have @Cacheable
 * See MetaMapSchemaSpec unit tests and rally-domain for how it works
 */
@Slf4j
@CompileStatic
class MetaMapSchemaService {

    OapiSupport oapiSupport

    MetaMapSchemaService(){
        oapiSupport = OapiSupport.instance()
    }

    //static cheater to get the bean, use sparingly if at all
    static MetaMapSchemaService bean(){
        AppCtx.get('metaMapSchemaService', this)
    }

    /**
     * get the MetaMapSchema from the MetaMapIncludes
     */
    @Cacheable('entityMapIncludes')
    MetaMapSchema getCachedMetaMapSchema(MetaMapIncludes mmi) {
        return MetaMapSchema.of(mmi)
    }

}
