/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileStatic

import gorm.tools.support.ConfigAware
import grails.plugin.cache.Cacheable
import grails.util.GrailsClassUtils

/**
 * config object for restApi. lookup logic refactored here so we can do caching and reloading here at a later time
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
class RestApiConfig implements ConfigAware {

    /**
     * merges in missing props for includes map by looking in config and on the entity class.
     * if it exists in the restApi config then that wins and overrides the others
     *
     * @param controllerIncludes the includes that might be set in controller
     * @param controllerKey the name of the controller key in the restApi config
     * @param entityClass the entity class to look for statics on
     * @return
     */
    @Cacheable('restApiConfig.includes')
    Map getIncludes(String controllerKey, Class entityClass, Map mergeIncludes){
        def includesMap = [:] as Map<String, Object>
        if(mergeIncludes) includesMap.putAll(mergeIncludes)

        Map cfgIncs = config.getProperty("restApi.${controllerKey}.includes", Map)
        //if anything on config then overrite them
        if (cfgIncs) {
            includesMap.putAll(cfgIncs)
        }
        //use includes if set in domain class as the default 'get'
        if (!includesMap['get']) {
            List includesGet = GrailsClassUtils.getStaticPropertyValue(entityClass, 'includes') as List
            if (includesGet) includesMap['get'] = includesGet
        }
        if (!includesMap['picklist']) {
            List picklistIncludes = GrailsClassUtils.getStaticPropertyValue(entityClass, 'picklistIncludes') as List
            if (picklistIncludes) includesMap['picklist'] = picklistIncludes
        }
        return includesMap
    }

    /**
     * merges in missing props for includes map by looking in config and on the entity class.
     * if it exists in the restApi config then that wins and overrides the others
     *
     * @param controllerKey the name of the controller key in the restApi config
     * @param entityClass the entity class to look for statics on
     * @param mergeIncludes the includes that might be set in controller
     * @return
     */
    @Cacheable('restApiConfig.qSearchIncludes')
    List getQSearchIncludes(String controllerKey, Class entityClass, List mergeIncludes){
        def qIncludes = [] as List<String>
        //see if there is a config for it
        def cfgQSearch = config.getProperty("restApi.${controllerKey}.qSearch", List)
        if (cfgQSearch) {
            qIncludes.addAll(cfgQSearch)
        }
        else if(mergeIncludes){
            qIncludes.addAll(mergeIncludes)
        }
        else {
            // look on domain
            List qSearchFieldsStatic = GrailsClassUtils.getStaticPropertyValue(entityClass, 'qSearchIncludes') as List
            if (qSearchFieldsStatic) qIncludes.addAll(qSearchFieldsStatic)
        }
        return qIncludes
    }

}
