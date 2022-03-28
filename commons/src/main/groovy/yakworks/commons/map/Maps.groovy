/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.commons.lang.PropertyTools
import yakworks.commons.util.StringUtils

/**
 * Helpful methods for dealing with maps
 * some merge ideas take from https://gist.github.com/robhruska/4612278 and https://e.printstacktrace.blog/how-to-merge-two-maps-in-groovy/
 *
 * @author Joshua Burnett (@basejump)
 */
@Slf4j
@CompileStatic
class Maps {

    /**
     * Return the value of a nested path. Alias to PropertyTools.getProperty.
     *
     * Example Maps.getProperty(source, "x.y.z")
     *
     * @param source - The source object
     * @param property - the property
     * @return value of the specified property or null if any of the intermediate objects are null
     */
    static Object value(Map source, String property) {
        PropertyTools.getProperty(source, property)
    }

    /**
     * Deeply merges the contents of each Map in sources, merging from
     * "right to left" and returning the merged Map.
     *
     * Mimics 'extend()' functions often seen in JavaScript libraries.
     * Any specific Map implementations (e.g. TreeMap, LinkedHashMap)
     * are not guaranteed to be retained. The ordering of the keys in
     * the result Map is not guaranteed. Only nested maps will be
     * merged; primitives, objects, and other collection types will be
     * overwritten.
     *
     * The source maps will not be modified.
     *
     * If only 1 map is passed in then it just returns that without making a copy or modifying
     *
     * @return the new merged map
     */
    static Map merge(Map[] sources) {
        if (sources.length == 0) return [:]
        if (sources.length == 1) return sources[0]

        sources.inject([:]) { merged, source ->
            source.each { k, val ->
                def mergedVal = merged[k]
                if (( mergedVal == null || mergedVal instanceof Map ) && val instanceof Map) {
                    if(mergedVal == null) merged[k] = [:]
                    merged[k] = merge(merged[k] as Map, val as Map)
                } else if ((mergedVal == null || mergedVal instanceof Collection) && val instanceof Collection) {
                    if(mergedVal == null) merged[k] = []
                    merged[k] = (Collection)merged[k] + (Collection)val
                    //The list could be list of maps, so make sure they get copied
                    merged[k] = merged[k].collect{ item ->
                        return (item instanceof Map) ? merge([:], item) : item
                    }
                } else {
                    merged[k] = val
                }
            }
            return merged
        } as Map
    }

    static Map merge(List<Map> sources) {
        merge(sources as Map[])
    }

    /**
     * See clone,
     */
    static Map deepCopy(Map source) {
        clone(source)
    }

    /**
     * Does a "deep" clone of the Map. If a shallow clone is desired use whats built into most al Java Map impls.
     * see merge. it uses merge to do a deep copy of the map into a new Map
     *
     * @return the cloned map
     */
    static Map clone(Map source) {
        if(!source) return [:]
        merge([:], source)
    }

    /**
     * Does a "deep" clone of the Collection of Maps.
     * See clone
     *
     * @return the cloned map
     */
    static Collection<Map> clone(Collection<Map> listOfMaps) {
        if(!listOfMaps) return []
        listOfMaps.collect{ Maps.clone(it)}
    }

    /**
     * Deeply remove/prune all nulls and falsey` empty maps, lists and strings as well
     *
     * @param map the map to prune
     * @param pruneEmpty default:true set to false to keep empty maps, lists and strings
     * @return the pruned map
     */
    public static <K, V> Map<K, V> prune(Map<K, V> map, boolean pruneEmpty = true) {
        if(!map) return map
        map.collectEntries { k, v ->
            [k, v instanceof Map ? prune(v as Map, pruneEmpty) : v]
        }.findAll { k, v ->
            if(pruneEmpty){
                if (v instanceof List || v instanceof Map || v instanceof String) {
                    return v
                } else {
                    return v != null
                }
            } else {
                return v != null
            }

        } as Map<K, V>
    }

    /**
     * recursively removes the flattened spring keys in the form of foo[0] that the config creates for lists
     *
     * @returns a new copied map with the fixes
     */
    static Map removePropertyListKeys(Map<String, Object> cfgMap){
        // for deeply nested array  like `foo: [1,2]`config can transform to [foo[0]: 1, foo[1]: 2], without `foo: [1,2]` in final result
        // so need to find such values and transform back to array
        List<String> array = cfgMap.keySet().findAll{ (it.matches(/.*\[\d*\]/) && !it.contains('.')) && !cfgMap.keySet().contains(it.split('\\[')[0])} as List
        def newCfgMap = cfgMap.findAll {
            !it.key.matches(/.*\[\d*\]/) && !it.key.contains('.')
        } as Map<String, Object>
        if (array) {
            array.reverse().each{
                String key = it.split('\\[')[0]
                if (newCfgMap[key] instanceof List) {
                    (newCfgMap[key] as List).push(cfgMap[it])
                } else {
                    newCfgMap[key] = [cfgMap[it]]
                }
            }
        }
        for (String key : newCfgMap.keySet()) {
            def val = newCfgMap[key]
            if(val instanceof Map){
                newCfgMap[key] = removePropertyListKeys(val as Map)
            }
            if (val instanceof List) {
                newCfgMap[key] = val.collect{it instanceof Map ? removePropertyListKeys(it) : it}
            }
        }
        return newCfgMap
    }

    /**
     * Loosely test 2 maps for equality
     * asserts more or less that every keySet in [a: 1, b: 2] exists in [a: 1, b: 2, c: 3] which is true in this example
     * asserts more or less that subset:[a: 1, b: 2] == full:[a: 1, b: 2, c: 3]
     * mapContains([a: 1, b: 2], [a: 1, c: 3]) returns false
     * mapContains([a: 2, b: 2], [a: 1, b: 2]) also returns false
     * if subset is an empty map or null returns false
     *
     * @param full the map to look in
     * @param subset the subset of values to make sure are in the full
     * @param exclude optional list of keys to exclude from the subset
     * http://csierra.github.io/posts/2013/02/12/loosely-test-for-map-equality-using-groovy/
     */
    static boolean mapContains(Map full, Map subset, List<String> exclude=[]) {
        //println "subset: $subset"
        //println "full: $full"
        if(!subset) return false
        return subset.findAll{
            !exclude.contains(it.key)
        }.every {
            def val = it.value
            if(val instanceof Map){
                return mapContains(full[it.key] as Map, val)
            } else {
                return val == full[it.key]
            }
        }
    }

    /**
     * checks that main map contains all the subset
     */
    static boolean containsAll(Map main, Map subset){
        main.entrySet().containsAll(subset.entrySet())
    }


    /**
     * Retrieves a boolean value from a Map for the given key
     *
     * @param key The key that references the boolean value
     * @param map The map to look in
     * @param defaultReturn if its doesn't have the key or map is null this is the default return value
     * @return A boolean value which will be false if the map is null, the map doesn't contain the key or the value is false
     */
    @SuppressWarnings('EmptyCatchBlock')
    static boolean getBoolean(String key, Map<?, ?> map, boolean defaultValue = false) {
        if (map == null) return defaultValue

        if (map.containsKey(key)) {
            Object o = map.get(key)
            if (o == null) return false
            if (o instanceof Boolean) {
                return (Boolean)o
            }
            try {
                if (o != null) {
                    return StringUtils.toBoolean(o.toString())
                }
            }
            catch (Exception e) {
                /* swallow exception and will return default */
            }
        }
        return defaultValue
    }

    static boolean 'boolean'(Map<?, ?> map, String key, boolean defaultValue = false) {
        return getBoolean(key, map, defaultValue)
    }

    /**
     * returns map with specific keys
     */
    public static <K, V> Map<K, V> pick(Map<K, V> map, Collection<K> keys){
        if(!map) return [:]
        map.subMap(keys)
    }

    /**
     * returns map with keys excluded
     */
    public static <K, V> Map<K, V> omit(Map<K, V> map, Collection<K> excludeKeys){
        if(!map) return [:]
        def keys = map.keySet().findAll{ !(it in excludeKeys)}
        map.keySet()
        return map.subMap(keys)
    }

}
