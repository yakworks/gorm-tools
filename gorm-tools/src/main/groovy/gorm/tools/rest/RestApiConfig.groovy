/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import grails.config.Config
import grails.core.GrailsApplication
import grails.util.GrailsClassUtils

/**
 * config object for restApi. lookup logic refactored here so we can do caching and reloading here at a later time
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
class RestApiConfig {
    @Autowired
    GrailsApplication grailsApplication

    //cache key is the api controller name which is the endpoint
    Map includesCache = new ConcurrentHashMap<String, Map<String, Object>>()
    Map qsearchCache = new ConcurrentHashMap<String, List<String>>()
    boolean _qSearchConfigChecked = false

    Config getConfig(){
        return grailsApplication.config
    }

    /**
     * merges in missing props for includes map by looking in config and on the entity class.
     * if it exists in the restApi config then that wins and overrides the others
     *
     * @param controllerIncludes the includes that might be set in controller
     * @param controllerKey the name of the controller key in the restApi config
     * @param entityClass the entity class to look for statics on
     * @return
     */
    Map getIncludes(String controllerKey, Class entityClass, Map mergeIncludes){
        def includesMap = ( includesCache[controllerKey] ?: [:]) as Map<String, Object>
        //for now use __configMerged__ to check
        if(includesMap.isEmpty() || !includesMap['__configMerged__']){
            // only 1 thread should be in here at a time
            synchronized (includesCache) {
                //start with mergeIncludes
                if(mergeIncludes) includesMap.putAll(mergeIncludes)

                Map cfgIncs = grailsApplication.config.getProperty("restApi.${controllerKey}.includes", Map)
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
                includesMap['__configMerged__'] = true //mark it so we don't check config again each time
                includesCache[controllerKey] = includesMap
            }
        }
        return includesMap
    }

    /**
     * merges in missing props for includes map by looking in config and on the entity class.
     * if it exists in the restApi config then that wins and overrides the others
     *
     * @param controllerIncludes the includes that might be set in controller
     * @param controllerKey the name of the controller key in the restApi config
     * @param entityClass the entity class to look for statics on
     * @return
     */
    List getQSearchIncludes(String controllerKey, Class entityClass, List mergeIncludes){
        def qIncludes = ( qsearchCache[controllerKey] ?: []) as List<String>
        //for now use __configMerged__ to check
        if(qIncludes.isEmpty() && !_qSearchConfigChecked){
            // only 1 thread should be in here at a time
            synchronized (qsearchCache) {
                //see if there is a config for it
                def cfgQSearch = grailsApplication.config.getProperty("restApi.${controllerKey}.qSearch", List)
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
                _qSearchConfigChecked = true
                qsearchCache[controllerKey] = qIncludes
            }
        }
        return qIncludes
    }
}
