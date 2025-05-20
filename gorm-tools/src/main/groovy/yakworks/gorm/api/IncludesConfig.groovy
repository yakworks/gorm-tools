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
    @Autowired IncludesConfig self //inject self so can call methods and get caching

    //static cheater to get the bean, use sparingly if at all
    static IncludesConfig bean(){
        AppCtx.get('includesConfig', this)
    }

    /**
     * Gets the includes/includesKey from the qParams or from the fallbackKeys
     * @return the list of fields in our mango format.
     */
    List<String> getIncludes(Map qParams, List fallbackIncludesKeys, Class entityClass) {
        //parse the params into the IncludesProps
        var incProps = IncludesProps.of(qParams).fallbackKeys(fallbackIncludesKeys)

        //if includes was passed in, then it wins
        if(incProps.includes) return incProps.includes

        //otherwise search based on includesKey
        List<String> incs = self.findIncludes(entityClass, incProps)
        return incs
    }

    @Cacheable(cacheNames="includesConfig.includesByClass",key="{#entityClass}")
    Map getIncludesMap(Class entityClass){
        Map includesMap = getClassStaticIncludes(entityClass)
        //if anything in yml config then they win
        Map includesConfigMap = apiConfig.getIncludesForEntity(entityClass.name)
        if(includesConfigMap) includesMap.putAll(includesConfigMap)
        return includesMap
    }

    /**
     * pulls the value for key from includes map.
     * Does NOT fallback to anything. If its not in the map then it returns empty list
     * @return the list or empty if not found
     */
    List<String> getByKey(Class entityClass, Object key){
        Map incsMap = self.getIncludesMap(entityClass) ?: [:]
        return (incsMap[key.toString()] ?: []) as List<String>
    }

    /**
     * Looks up the key on statics and api yml.
     * get the fields includes with a list of keys to search in order, stopping at the first found.
     * If nothing found will return ['*']
     * @param entityClass the entity class to lookup
     * @param includesKey the key to look for
     * @return the includes for key, or the 'get' includes or ['*']
     */
    List<String> findByKeys(Class entityClass, List includesKeys){
        def incMap = self.getIncludesMap(entityClass)
        List<String> incs = searchMapForKeys(incMap, includesKeys)
        return incs ?: ['*']
    }

    /**
     * finds the right includes.
     *   - looks for includes param and uses that if passed in
     *   - looks for includesKey param and uses that if set, falling back to the fallbackKeys
     *   - the fallbackKeys will itself unlimately fallback to the 'get' includes if it can't be found
     *
     * @return the List of includes field that can be passed to the MetaMap creation
     */
    List<String> findIncludes(Class entityClass, IncludesProps incProps){
        //if it has includes then that always wins, just return right away
        if(incProps.includes) return incProps.includes
        //use keys to search
        return findByKeys(entityClass, incProps.includesKeys)
    }

    /**
     * get the fields includes with a list of keys to search in order, stopping at the first found
     * @param includesMap the map to check
     * @param includesKeys the key to check
     * @return the first match or empty if nothing
     */
    List<String> searchMapForKeys(Map includesMap, List includesKeys){
        for(Object key : includesKeys){
            String skey = key.toString()
            if(includesMap.containsKey(skey)) {
                def incs = includesMap[skey]
                return incs as List<String>
            }
        }
        return []
    }


    /**
     * Get the statics includes on the class
     */
    protected Map getClassStaticIncludes(Class entityClass) {
        // look for includes map on the domain first
        Map entityIncludes = ClassUtils.getStaticPropertyValue(entityClass, 'includes', Map)
        Map includesMap = (entityIncludes ? Maps.clone(entityIncludes) : [:]) as Map<String, Object>
        return includesMap
    }

    /**
     * load class
     */
    static Class lookupClass(String entityClassName){
        ClassLoader classLoader = IncludesConfig.getClassLoader()
        Class entityClass = classLoader.loadClass(entityClassName)
        return entityClass
    }

}
