/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileStatic

import gorm.tools.support.ConfigAware
import grails.plugin.cache.Cacheable
import yakworks.commons.lang.ClassUtils
import yakworks.commons.map.Maps

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
     * @param includesOverrides passed in from controller, overrrides for whats in config and domain
     */
    @Cacheable('restApiConfig.includes')
    Map getIncludes(String controllerKey, String namespace, Class entityClass, Map mergeIncludes){
        // look for includes map on the domain first
        Map entityIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'includes', Map)
        Map includesMap = ( entityIncludes ? Maps.deepCopy(entityIncludes) : [:] ) as Map<String, Object>

        Map pathConfig = getPathConfig(controllerKey, namespace)

        //if anything on config then overrite them
        if (pathConfig?.includes) {
            includesMap.putAll(pathConfig.includes as Map)
        }

        if(mergeIncludes) includesMap.putAll(mergeIncludes)

        return includesMap
    }

    Map getPathConfig(String controllerKey, String namespace){
        String configPath = namespace ? "restApi.paths.${namespace}.${controllerKey}" : "restApi.paths.${controllerKey}"
        // String pathkey = namespace ? "${namespace}/${controllerKey}" : controllerKey
        // //println "getting restApi key ${pathkey}"
        // return restApiConfig[pathkey] as Map
        Map pathConfig = config.getProperty(configPath, Map)
        if(pathConfig == null && namespace){
            //try the other way
            Map apiConfigs = config.getProperty('restApi.paths', Map)
            String pathkey = "${namespace}/${controllerKey}"
            pathConfig = apiConfigs.containsKey(pathkey) ? apiConfigs[pathkey] as Map : null
        }
        return pathConfig
    }

    Map getPathConfig(String pathkey){
        Map pathConfig
        if(pathkey.contains('_')){
            String[] parts = pathkey.split('[_]')
            pathConfig = getPathConfig(parts[1], parts[0])
        } else {
            pathConfig = getPathConfig(pathkey, null)
        }
        return pathConfig
    }

}
