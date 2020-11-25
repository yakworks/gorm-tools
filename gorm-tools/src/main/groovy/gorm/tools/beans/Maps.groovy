/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Helpful methods for dealing with maps
 */

@Slf4j
@CompileStatic
class Maps {

    /**
     * https://gist.github.com/robhruska/4612278
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
     */
    static Map merge(Map[] sources) {
        if (sources.length == 0) return [:]
        if (sources.length == 1) return sources[0]

        sources.inject([:]) { result, source ->
            source.each { k, v ->
                result[k] = result[k] instanceof Map ? merge(result[k] as Map, v as Map) : v
            }
            result
        } as Map
    }

    /**
     * Deeply remove/prune all nulls and falsey` empty maps, lists and strings as well
     *
     * @param map the map to prune
     * @param pruneEmpty default:true set to false to keep empty maps, lists and strings
     * @return the pruned map
     */
    static Map prune(Map map, boolean pruneEmpty = true) {
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

        }
    }

    /**
     * recursively removes the flattened spring keys in the form of foo[0] that the config creates for lists
     *
     * @returns a new copied map with the fixes
     */
    static Map removePropertyListKeys(Map<String, Object> cfgMap){
        def newCfgMap = cfgMap.findAll {
            !it.key.matches(/.*\[\d*\]/)
        } as Map<String, Object>

        for (String key : newCfgMap.keySet()) {
            def val = newCfgMap[key]
            if(val instanceof Map){
                newCfgMap[key] = removePropertyListKeys(val as Map)
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

}
