/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.map


import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import yakworks.commons.lang.IsoDateUtil

/**
 * The primary use of this is to convert a net json tree to a flat map that
 * can be used with the old grails parser or can be used in CSV, where keys or header is expected to
 * look like `foo.bar: 'val` for an object like `foo:[bar: 'val']`
 * MapFlattener taken from here https://github.com/dmillett/jConfigMap
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class MapFlattener {

    Map<String, Object> target
    private final KeyVersion keyVersion = new KeyVersion()
    boolean convertEmptyStringsToNull = true
    boolean convertObjectToString = false

    /**
     * Groovy transforms JSON to either a Map or List based on the root node.
     */
    static MapFlattener of(Map<String, Object> target) {
        new MapFlattener().target(target)
    }

    /**
     * Groovy transforms JSON to either a Map or List based on the root node.
     */
    static Map<String, Object> flattenMap(Map<String, Object> target) {
        new MapFlattener().target(target).flatten()
    }

    /**
     * flatten  the target map
     */
    Map<String, Object> flatten() {
        flatten(target)
    }
    /**
     * Groovy transforms JSON to either a Map or List based on the root node.
     */
    Map<String, Object> flatten(Object mapToFlatten) {

        Map<String, Object> keyValues = [:]

        if (mapToFlatten == null) {
            return keyValues
        }

        if (mapToFlatten instanceof Map) {
            keyValues.putAll(transformGroovyJsonMap((Map) mapToFlatten, ""))
        } else if (mapToFlatten instanceof List) {
            keyValues.putAll(transformJsonArray((List) mapToFlatten, ""))
        }

        return keyValues
    }

    /**
     * Iterates through each Map entry and transforms any sub-maps or sub-arrays
     * therein. Otherwise, it is just a string "key" and "value".
     */
    @SuppressWarnings(['EmptyCatchBlock'])
    Map<String, Object> transformGroovyJsonMap(Map jsonMap, String currentName) {

        if (jsonMap == null || jsonMap.isEmpty()) {
            return [:]
        }

        Map<String, Object> keyValues = [:]

        jsonMap.each {  Map.Entry entry ->

            String key = String.valueOf(entry.key)
            if (currentName != null && !currentName.empty) {
                key = currentName + "." + key
            }
            //if it is an association id, then set value to 'null' to set the association to null
            if ((key && key.toString().endsWith(".id")) && (entry.value == null || entry.value.toString() == 'null' || entry.value.toString().trim() == "")) {
                keyVersion.updateMapWithKeyValue(keyValues, key, "null")
            } else if (entry.value == null || entry.value?.toString() == 'null') {
                keyVersion.updateMapWithKeyValue(keyValues, key, null)
            } else if (entry.value instanceof List) {
                Map<String, Object> jsonListKeyValues = transformJsonArray(entry.value as List, key)
                keyValues.putAll(jsonListKeyValues)
            } else if (entry.value instanceof Map) {
                Map<String, Object> jsonMapKeyValues = transformGroovyJsonMap(entry.value as Map, key)
                keyValues.putAll(jsonMapKeyValues)
            } else if (entry.value instanceof CharSequence) {
                doString(keyValues, key, entry.value)
            }
            else {
                if(convertObjectToString) {
                    doString(keyValues, key, entry.value)
                } else {
                    keyVersion.updateMapWithKeyValue(keyValues, key, entry.value)
                }
            }
        }

        return keyValues
    }

    String doString(Map<String, Object> keyValues, String key, Object value){
        if(IsoDateUtil.isDate(value)){
            def sdate= IsoDateUtil.format(value)
            keyVersion.updateMapWithKeyValue(keyValues, key, sdate)
            return sdate
        }
        String v = String.valueOf(value)
        v = v.trim()
        if ("" == v && convertEmptyStringsToNull) {
            v = null
        }
        keyVersion.updateMapWithKeyValue(keyValues, key, v)
        return v
    }

    /**
     * Flatten Groovy-JSON Array objects
     */
    Map<String, Object> transformJsonArray(List jsonArray, String currentName) {

        if (jsonArray == null || jsonArray.empty) {
            return [:]
        }

        Map keyValues = [:]
        keyValues.put(currentName, jsonArray)

        int index = 0

        jsonArray.each { jsonElement ->
            String arrayName = [currentName, index++].join('.')
            if (jsonElement == null) {
                keyValues.put(arrayName, null)
            } else if (jsonElement instanceof Map) {
                Map<String, Object> jsonMapKeyValues = transformGroovyJsonMap(jsonElement as Map, arrayName)
                keyVersion.updateMapWithKeyValues(keyValues, jsonMapKeyValues)
            } else if (jsonElement instanceof List) {
                Map<String, Object> jsonArrayKeyValues = transformJsonArray(jsonElement as List, arrayName)
                keyVersion.updateMapWithKeyValues(keyValues, jsonArrayKeyValues)
            } else {
                String value = String.valueOf(jsonElement)
                keyVersion.updateMapWithKeyValue(keyValues, arrayName, value)
            }
        }

        return keyValues as Map<String, Object>
    }

}

@CompileStatic
class KeyVersion {

    private final Map<String, Integer> keyVersionCount = [:]

    void updateMapWithKeyValue(Map<String, Object> originalMap, String key, Object value) {
        if (keyVersionCount.containsKey(key)) {
            String indexedKey = buildIndexedKeyAndUpdateKeyCount(key)
            originalMap.put(indexedKey, value)
        } else {
            originalMap.put(key, value)
        }
    }

    void updateMapWithKeyValues(Map<String, Object> originalMap, Map<String, Object> additionalMap) {

        additionalMap.entrySet().each { entry ->

            String downcaseKey = entry.key
            if (originalMap.containsKey(downcaseKey)) {
                String indexedKey = buildIndexedKeyAndUpdateKeyCount(downcaseKey)
                originalMap.put(indexedKey, entry.value)
            } else {
                originalMap.put(downcaseKey, entry.value)
            }
        }
    }

    Map buildMapFromOriginal(Map original, Map<String, Object> additional) {

        Map combinedMap = [:] as Map<String, Object>
        combinedMap.putAll(original)
        updateMapWithKeyValues(combinedMap, additional)

        return combinedMap
    }

    private String buildIndexedKeyAndUpdateKeyCount(String key) {

        String downcaseKey = key
        String indexedKey = key

        if (keyVersionCount.containsKey(key)) {
            Integer keyIndex = keyVersionCount.get(downcaseKey) + 1
            indexedKey = key + "." + keyIndex
            keyVersionCount.put(downcaseKey, keyIndex)
        } else {
            indexedKey = downcaseKey + "." + 1
            keyVersionCount.put(downcaseKey, 1)
        }

        return indexedKey
    }
}
