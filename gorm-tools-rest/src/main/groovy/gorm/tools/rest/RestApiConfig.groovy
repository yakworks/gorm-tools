/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileStatic

import gorm.tools.support.ConfigAware
import grails.plugin.cache.Cacheable
import yakworks.commons.lang.ClassUtils

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
     */
    @Cacheable('restApiConfig.includes')
    Map getIncludes(String controllerKey, String namespace, Class entityClass, Map mergeIncludes){
        def includesMap = [:] as Map<String, Object>
        if(mergeIncludes) includesMap.putAll(mergeIncludes)

        Map pathConfig = getPathConfig("${namespace}/${controllerKey}")
        Map cfgIncs = pathConfig.includes as Map

        //if anything on config then overrite them
        if (cfgIncs) {
            includesMap.putAll(cfgIncs)
        }
        //use includes if set in domain class as the default 'get'
        if (!includesMap['get']) {
            List includesGet = ClassUtils.getStaticPropertyValue(entityClass, 'includes', List)
            if (includesGet) includesMap['get'] = includesGet
        }
        if (!includesMap['picklist']) {
            List picklistIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'picklistIncludes', List)
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
     */
    @Cacheable('restApiConfig.qSearchIncludes')
    List getQSearchIncludes(String controllerKey, String namespace, Class entityClass, List mergeIncludes){
        def qIncludes = [] as List<String>
        //see if there is a config for it
        Map pathConfig = getPathConfig("${namespace}/${controllerKey}")
        def cfgQSearch = pathConfig.qSearch as List
        if (cfgQSearch) {
            qIncludes.addAll(cfgQSearch)
        }
        else if(mergeIncludes){
            qIncludes.addAll(mergeIncludes)
        }
        else {
            // look on domain
            List qSearchFieldsStatic = ClassUtils.getStaticPropertyValue(entityClass, 'qSearchIncludes', List)
            if (qSearchFieldsStatic) qIncludes.addAll(qSearchFieldsStatic)
        }
        return qIncludes
    }

    Map getPathConfig(String pathKey){
        def restApiConfig = config.getProperty("restApi.paths", Map)
        pathKey = pathKey.replace('_','/')
        println "getting restApi key ${pathKey}"
        return restApiConfig[pathKey] as Map
    }

}
