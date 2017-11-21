package gorm.tools.mango

class MangoTidyMap {

    static Map tidy(Map map) {
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
    static Map pathToMap(String path, Object val, Map map) {
        if (path.contains(".")) {
            String newKey = path.split("[.]")[0]
            if (!map[newKey]) map[newKey] = [:]
            pathToMap(path.split("[.]").tail().join("."), val, map[newKey] as Map)
        } else {
            if (!map[path]) map[path] = [:]
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

    static Map toMangoOperator(Map map, Map result = [:]) {
        map.each { key, val ->
            result[key] = [:]

            if (['$or', '$and'].contains(key) && val instanceof Map){
                result[key]= val.collect{k,v-> toMangoOperator(["$k": v])}
                return
            }
            if (val instanceof Map) {
                toMangoOperator(val, result[key] as Map)
            } else {
                if (key.toString().startsWith('$')) {result[key] = val; return} //if we already have Mango method
                if (val instanceof List) {
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
                if(['$isNull', '$isNotNull'].contains(val)) {
                    result[key][val] = true
                } else {
                    result[key]['$eq'] = val
                }
            }

        }
        result

    }

}
