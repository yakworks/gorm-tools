/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic

import gorm.tools.beans.AppCtx
import gorm.tools.support.ConfigAware
import grails.plugin.cache.Cacheable
import yakworks.commons.lang.ClassUtils
import yakworks.commons.map.Maps

/**
 * Helper to lookup includes for map or list based api, usually a json and rest based api.
 * look on the static includes field of the Class first and look for config overrides
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
class IncludesConfig implements ConfigAware {

    static final Map<String, String> EntityClassPathKeys = new ConcurrentHashMap<String, String>()

    //static cheater to get the bean, use sparingly if at all
    static IncludesConfig bean(){
        AppCtx.get('includesConfig', this)
    }

    /**
     * pulls the value for key from includes map
     */
    List<String> getIncludes(Class entityClass, String key){
        Map incsMap = getIncludes(entityClass)
        return (incsMap ? incsMap[IncludesKey.qSearch.name()] : []) as List<String>
    }

    @Cacheable('apiConfig.includes')
    Map getIncludes(Class entityClass){
        Map includesMap = getClassStaticIncludes(entityClass)
        Map pathConfig = findConfigByEntityClass(entityClass.name)
        //if anything on config then overrite them
        if (pathConfig?.includes) {
            includesMap.putAll(pathConfig.includes as Map)
        }
        return includesMap
    }

    /**
     * merges in missing props for includes map by looking in config and on the entity class.
     * if it exists in the restApi config then that wins and overrides the others
     *
     * @param entityKey the name of the controller key in the restApi config
     * @param namespace the namespace it falls under
     * @param entityClass the entity class to look for statics on
     * @param mergeIncludes may be passed in from controller etc, provides overrrides for whats in config and domain
     */
    @Cacheable('apiConfig.includes')
    Map getIncludes(String entityKey, String namespace, Class entityClass, Map mergeIncludes){
        // look for includes map on the domain first
        Map entityIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'includes', Map)
        Map includesMap = getClassStaticIncludes(entityClass)

        Map pathConfig = getPathConfig(entityKey, namespace)

        //if anything on config then overrite them
        if (pathConfig?.includes) {
            includesMap.putAll(pathConfig.includes as Map)
        }

        if(mergeIncludes) includesMap.putAll(mergeIncludes)

        return includesMap
    }

    Map getClassStaticIncludes( Class entityClass) {
        // look for includes map on the domain first
        Map entityIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'includes', Map)
        Map includesMap = (entityIncludes ? Maps.deepCopy(entityIncludes) : [:]) as Map<String, Object>
        return includesMap
    }
    /**
     * gets the Map config for the entityKey and namespace
     */
    Map getPathConfig(String entityKey, String namespace){
        String configPath = namespace ? "api.paths.${namespace}.${entityKey}" : "api.paths.${entityKey}"
        Map pathConfig = config.getProperty(configPath, Map)
        if(pathConfig == null && namespace){
            //try the other way where key has namespace in it
            Map apiConfigs = config.getProperty('api.paths', Map)
            String pathkey = "${namespace}/${entityKey}"
            pathConfig = apiConfigs.containsKey(pathkey) ? apiConfigs[pathkey] as Map : null
        }

        //if nothing and it has a dot than entity key might be full class name with package, so do search
        if(!pathConfig && entityKey.indexOf('.') != -1)
            pathConfig = findConfigByEntityClass(entityKey)

        return pathConfig
    }

    /**
     * looks for the entityClass key that matches the className
     */
    Map findConfigByEntityClass(String className){
        if(EntityClassPathKeys.isEmpty()){
            setupEntityClassPathKeys()
        }
        String rootCfgKey = EntityClassPathKeys[className]
        return rootCfgKey ? config.getProperty(rootCfgKey, Map) : [:]
    }

    /**
     * setup pathKeys for entityClass
     * this scans the api.paths base for entityClassName and store the pathKey with namespace.
     * when it finds  api.paths.security.user.entityClass: gorm.tools.security.domain.AppUser
     * it will store ['gorm.tools.security.domain.AppUser': 'api.paths.security.user'] for faster lookup
     */
    void setupEntityClassPathKeys() {
        Properties cfgProps = config.toProperties()
        Set keySet = cfgProps.keySet() as Set<String>
        Set entClassKeySets = keySet.findAll{ it.matches(/api\.paths.*\.entityClass/) }
        for(String ckey: entClassKeySets){
            String entityClass = cfgProps.getProperty(ckey)
            String rootCfgKey = ckey.replace(".entityClass", '')
            EntityClassPathKeys[entityClass] = rootCfgKey
        }
    }

    /**
     * FIXME whats this for? can we remove or is it used?
     */
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

    /**
     * get the fields includes with a list of keys to search in order, stopping at the first found
     * and falling back to 'get' which should always exist
     *
     * @param includesKeys the List of keys to get in order of priority
     * @return the includes list that can be passed into MetMap creation
     */
    static List<String> getFieldIncludes(Map includesMap, List<String> includesKeys){
        if(!includesKeys.contains('get')) includesKeys << 'get'
        for(String key : includesKeys){
            if(includesMap.containsKey(key)) return includesMap[key] as List<String>
        }
        //its should never get here but in case it does and config is messed then fall back *
        return ['*']
    }

}
