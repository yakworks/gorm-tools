/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Utils to normalizes params map to transform it to mango language
 */
@CompileStatic
class MangoTidyMap {

    /**
     * Transforms passed params map to normalized mango criteria map
     *
     * @param map params that should be transformed to mango language
     * @return normalized mango map
     */
    static Map tidy(Map<String, Object> map) {
        Map nested = [:]
        map.each { String k, Object v ->
            pathToMap(k, v, nested)
        }
        toMangoOperator(nested)
    }

    /**
     * Extends the map with nested value by specific path
     * so pathToMap("a.b.c", 1, [:]) -> [a:[b:[c:1]]]
     * or pathToMap("a.b.c", 1, [d:2]) -> [a:[b:[c:1]], d:2]
     *
     * @param path path in the nested map where value should be placed
     * @param val value that should be added to the nested map
     * @param map map that should be extended with the nested value
     * @return extended map
     */
    @CompileDynamic
    static Map pathToMap(String path, Object val, Map map) {
        if (path.contains(".")) {
            String newKey = path.split("[.]")[0]
            if (!map[newKey]) map[newKey] = [:]
            pathToMap(path.split("[.]").tail().join("."), val, map[newKey] as Map)
        } else {
            if (!map[path]) map[path] = [:]
            //we should check if nested values have composed keys("customer.address.id")
            if (val instanceof Map) {
                val.each { k, v ->
                    pathToMap(k as String, v, map[path] as Map)
                }
            } else {
                map[path] = val
            }
        }
        map
    }

    /**
     * Adds mango operators based on values types
     *
     * @param map params that should be extended with mango operators
     * @param result map that should contain mango results
     * @return map with mango criteria params
     */
    @CompileDynamic
    static Map toMangoOperator(Map map, Map result = [:]) {
        map.each { key, val ->
            result[key] = [:]
            if (MangoBuilder.junctionOps.keySet().contains(key)) {
                if (val instanceof Map) {
                    result[key] = val.collect { k, v -> tidy([(k.toString()): v]) }
                    return
                }

                if (val instanceof List) {
                    result[key] = val.collect { v -> tidy(['$and': v]) }
                    return
                }
            }
            if (val instanceof Map && !MangoBuilder.sortOps.keySet().contains(key)) {
                toMangoOperator(val, result[key] as Map)
            } else {
                if (key.toString().startsWith('$')) {
                    result[key] = val; return
                } //if we already have Mango method
                if (val instanceof List) {
                    // for handling case {customer: [{id:1}, {id:2}]}, transforms to {customer:{id:{'$in': [1,2]}}}
                    if (val[0] instanceof Map) {
                        result[key]["${val[0].keySet()[0]}"] = ['$in': val.collect { it.values()[0] }]
                        return
                    }
                    result[key]['$in'] = val
                    return
                }
                if (val instanceof String && val.contains("%")) {
                    result[key]['$ilike'] = val
                    return
                }
                if (MangoBuilder.existOps.keySet().contains(val)) {
                    result[key][val] = true
                } else {
                    result[key]['$eq'] = val
                }
            }
        }
        result

    }

}
