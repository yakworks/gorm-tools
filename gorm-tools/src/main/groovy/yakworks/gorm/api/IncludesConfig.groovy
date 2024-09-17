/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable

import yakworks.commons.lang.ClassUtils
import yakworks.commons.map.Maps
import yakworks.spring.AppCtx

/**
 * Helper to lookup includes for map or list based api, usually a json and rest based api.
 * look on the static includes field of the Class first and look for config overrides
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
class IncludesConfig {

    @Autowired ApiConfig apiConfig

    //static cheater to get the bean, use sparingly if at all
    static IncludesConfig bean(){
        AppCtx.get('includesConfig', this)
    }

    /**
     * pulls the value for key from includes map.
     * @return the list or empty if not found
     */
    List<String> getIncludes(Class entityClass, String key){
        Map incsMap = getIncludes(entityClass)
        return (incsMap ? incsMap[key] : []) as List<String>
    }

    @Cacheable('ApiConfig.includesByClass')
    Map getIncludes(Class entityClass){
        Map includesMap = getClassStaticIncludes(entityClass)
        //if anything in yml config then they win
        Map includesConfigMap = apiConfig.getIncludesForEntity(entityClass.name)
        if(includesConfigMap) includesMap.putAll(includesConfigMap)
        return includesMap
    }

    /**
     * Get the map of includes
     * @param entityClassName fully qualified class name, not short name
     * @return the include map
     */
    Map getIncludes(String entityClassName){
        ClassLoader classLoader = getClass().getClassLoader()
        Class entityClass = classLoader.loadClass(entityClassName)
        return getIncludes(entityClass)
    }

    /**
     * gets using getFieldIncludes which ensures it always has something as it falls back to the get [*] if not found
     */
    List<String> getIncludesByKey(String entityClassName, String includesKey){
        def incMap = getIncludes(entityClassName)
        return getFieldIncludes(incMap, [includesKey])
    }

    /**
     * finds the right includes.
     *   - looks for includes param and uses that if passed in
     *   - looks for includesKey param and uses that if set, falling back to the defaultIncludesKey
     *   - falls back to the passed fallbackKeys if not set
     *   - the fallbackKeys will itself unlimately fallback to the 'get' includes if it can't be found
     *
     * @param params the request params
     * @return the List of includes field that can be passed to the MetaMap creation
     */
    List<String> findIncludes(String entityClassName, Map params, List<String> fallbackKeys = []){
        return findIncludes(entityClassName, IncludesProps.of(params), fallbackKeys)
    }

    List<String> findIncludes(String entityClassName, IncludesProps incProps, List<String> fallbackKeys = []){
        List<String> keyList = []
        //if it has a includes then just parse that and pass it back
        if(incProps?.includes) {
            return incProps.includes
        } else if(incProps?.includesKey){
            keyList << incProps.includesKey
        }
        keyList.addAll(fallbackKeys)
        def incMap = getIncludes(entityClassName)
        return getFieldIncludes(incMap, keyList)
    }

    Map getClassStaticIncludes( Class entityClass) {
        // look for includes map on the domain first
        Map entityIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'includes', Map)
        Map includesMap = (entityIncludes ? Maps.clone(entityIncludes) : [:]) as Map<String, Object>
        return includesMap
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
            if(includesMap.containsKey(key)) {
                def incs = includesMap[key]
                //they get merged in as Maps when coming from external configs with the key being index, [0:id, 1:name, etc..], just need vals
                return (incs instanceof Map ? incs.values() : incs ) as List<String>
            }
        }
        //its should never get here but in case it does and config is messed then fall back *
        return ['*']
    }

}
