package gorm.tools.beans

import groovy.transform.CompileStatic

/**
 * MapFlattener taken from here https://github.com/dmillett/jConfigMap
 *
 *
 * @author dmillett
 *
 * Copyright 2011 David Millett
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * The primary use of this is to convert a net json tree to a flat map that
 * can eb used with the old grails parser
 */
@CompileStatic
class MapFlattener {

    private final KeyVersion keyVersion = new KeyVersion()
    boolean convertEmptyStringsToNull = true

    /**
     * Groovy transforms JSON to either a Map or List based on the root node.
     *
     * @param groovyJsonObject
     * @return A Map of String,String
     */
    Map<String, String> flatten(groovyJsonObject) {

        Map<String, String> keyValues = [:]

        if (groovyJsonObject == null) {
            return keyValues
        }

        if (groovyJsonObject instanceof Map) {
            keyValues.putAll(transformGroovyJsonMap((Map)groovyJsonObject, ""))
        } else if (groovyJsonObject instanceof List) {
            keyValues.putAll(transformJsonArray((List)groovyJsonObject, ""))
        }

        return keyValues
    }

    /**
     * Iterates through each Map entry and transforms any sub-maps or sub-arrays
     * therein. Otherwise, it is just a string "key" and "value".
     *
     * @param jsonMap
     * @param currentName
     * @return
     */
    Map<String, String> transformGroovyJsonMap(Map jsonMap, String currentName) {

        if (jsonMap == null || jsonMap.isEmpty()) {
            return [:]
        }

        Map<String, String> keyValues = [:]

        jsonMap.each { entry ->

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
                Map<String, String> jsonListKeyValues = transformJsonArray(entry.value as List, key)
                keyValues.putAll(jsonListKeyValues)
            } else if (entry.value instanceof Map) {
                Map<String, String> jsonMapKeyValues = transformGroovyJsonMap(entry.value as Map, key)
                keyValues.putAll(jsonMapKeyValues)
            } else {
                String value = String.valueOf(entry.value)

                if (value != null) {
                    value = value.trim() //trim strings - same as grails.databinding.trimStrings
                }
                //convert empty strings to null - same behavior as grails.databinding.convertEmptyStringsToNull
                if ("" == value && convertEmptyStringsToNull) {
                    value = null
                }

                if (value != null && DateUtil.GMT_SECONDS.matcher(value).matches()) {
                    //FIXME dirty hack!!!
                    //XXX why did we use default format with trimmed time?
                    value = DateUtil.parseJsonDate(value).format("yyyy-MM-dd'T'hh:mm:ss'Z'")
                }
                keyVersion.updateMapWithKeyValue(keyValues, key, value)
            }
        }

        return keyValues
    }

    /**
     * Flatten Groovy-JSON Array objects
     *
     * @param jsonArray
     * @param currentName
     * @return A map of String,String
     */
    Map<String, String> transformJsonArray(List jsonArray, String currentName) {

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
                Map<String, String> jsonMapKeyValues = transformGroovyJsonMap(jsonElement as Map, arrayName)
                keyVersion.updateMapWithKeyValues(keyValues, jsonMapKeyValues)
            } else if (jsonElement instanceof List) {
                Map<String, String> jsonArrayKeyValues = transformJsonArray(jsonElement as List, arrayName)
                keyVersion.updateMapWithKeyValues(keyValues, jsonArrayKeyValues)
            } else {
                String value = String.valueOf(jsonElement)
                keyVersion.updateMapWithKeyValue(keyValues, arrayName, value)
            }
        }

        return keyValues
    }

}

@CompileStatic
class KeyVersion {

    private Map<String, Integer> keyVersionCount = [:]

    void updateMapWithKeyValue(Map<String, String> originalMap, String key, String value) {

        // if ( key == null || value == null )
        // {
        //     return
        // }

        //String downcaseKey = key.toLowerCase()
        if (keyVersionCount.containsKey(key)) {
            String indexedKey = buildIndexedKeyAndUpdateKeyCount(key)
            originalMap.put(indexedKey, value)
        } else {
            originalMap.put(key, value)
        }
    }

    void updateMapWithKeyValues(Map<String, String> originalMap, Map<String, String> additionalMap) {

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

    Map buildMapFromOriginal(Map original, Map additional) {

        Map combinedMap = [:]
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
