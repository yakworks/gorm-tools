/*
* Copyright 2004-2005 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.grails.datastore.mapping.model.config.GormProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import grails.util.TypeConvertingMap

/**
 * This is a copy of the GrailsParameterMap, primary to remove the need for HttpServletRequest.
 * Allows a flattened map of path keys such that
 * foo.bar.id:1, foo.amount:10 would end up as [foo: [bar: [id: 1]], amount:10]
 *
 * Orginal authors from GrailsParameterMap
 * @author Graeme Rocher
 * @author Lari Hotari
 * @since Oct 24, 2005\
 * TODO convert to groovy
 */
@SuppressWarnings(["rawtypes", "unchecked" ])
@CompileStatic
class PathKeyMap extends TypeConvertingMap implements Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(PathKeyMap)

    String pathDelimiter

    /**
     * Populates the PathKeyMap with supplied map.
     *
     * @param values The values to populate with
     */
    PathKeyMap(Map values) {
        this(values, ".")
    }

    // add class variable that we use it
    PathKeyMap(Map values, String pathDelimiter) {
        super()
        this.pathDelimiter = pathDelimiter
        if(values) {
            wrappedMap.putAll(values)
            updateNestedKeys(values)
        }
    }

    //need this, or else, groovy metaclass would call 'get' method of this class, resulting in StackOverflow error
    //See MetaClassImpl.getProperty
    protected Map getWrappedMap() {
        return this.@wrappedMap //direct field access
    }

    @Override
    Object clone() {
        if (wrappedMap.isEmpty()) {
            return new PathKeyMap([:], pathDelimiter)
        } else {
            Map clonedMap = new LinkedHashMap(wrappedMap)
            // deep clone nested entries
            for(Iterator it=clonedMap.entrySet().iterator(); it.hasNext();) {
                Entry entry = (Entry)it.next()
                if (entry.getValue() instanceof PathKeyMap) {
                    entry.setValue(((PathKeyMap)entry.getValue()).clone())
                }
            }
            return new PathKeyMap(clonedMap, pathDelimiter)
        }
    }

    void mergeValuesFrom(PathKeyMap otherMap) {
        wrappedMap.putAll((PathKeyMap)otherMap.clone())
    }

    @Override
    Object get(Object key) {
        // removed test for String key because there
        // should be no limitations on what you shove in or take out
        Object returnValue = null
        returnValue = wrappedMap.get(key)
        if (returnValue instanceof String[]) {
            String[] valueArray = (String[])returnValue
            if (valueArray.length == 1) {
                returnValue = valueArray[0]
            } else {
                returnValue = valueArray
            }
        }
        else if(returnValue == null && (key instanceof Collection)) {
            return DefaultGroovyMethods.subMap(wrappedMap, (Collection)key)
        }

        return returnValue
    }

    @Override
    Object put(Object key, Object value) {
        if (value instanceof CharSequence) value = value.toString()
        if (key instanceof CharSequence) key = key.toString()
        Object returnValue =  wrappedMap.put(key, value)
        if (key instanceof String) {
            String keyString = (String)key
            if (keyString.indexOf(pathDelimiter) > -1) {
                processNestedKeys(this, keyString, keyString, wrappedMap)
            }
        }
        return returnValue
    }

    @Override
    Object remove(Object key) {
        return wrappedMap.remove(key)
    }

    @Override
    void putAll(Map map) {
        for (Object entryObj : map.entrySet()) {
            Entry entry = (Entry)entryObj
            put(entry.getKey(), entry.getValue())
        }
    }


    /**
     * @return The identifier in the request
     */
    Object getIdentifier() {
        return get(GormProperties.IDENTITY)
    }

    protected void updateNestedKeys(Map keys) {
        for (Object keyObject : keys.keySet()) {
            String key = (String)keyObject
            processNestedKeys(keys, key, key, wrappedMap)
        }
    }

    /*
     * Builds up a multi dimensional hash structure from the parameters so that nested keys such as
     * "book.author.name" can be addressed like params['author'].name
     *
     * This also allows data binding to occur for only a subset of the properties in the parameter map.
     */
    private void processNestedKeys(Map requestMap, String key, String nestedKey, Map nestedLevel) {
        final int nestedIndex = nestedKey.indexOf(pathDelimiter)
        if (nestedIndex == -1) {
            return
        }

        // We have at least one sub-key, so extract the first element
        // of the nested key as the prfix. In other words, if we have
        // 'nestedKey' == "a.b.c", the prefix is "a".
        String nestedPrefix = nestedKey.substring(0, nestedIndex)

        // Let's see if we already have a value in the current map for the prefix.
        Object prefixValue = nestedLevel.get(nestedPrefix)
        if (prefixValue == null) {
            // No value. So, since there is at least one sub-key,
            // we create a sub-map for this prefix.

            prefixValue = new PathKeyMap([:], pathDelimiter)
            nestedLevel.put(nestedPrefix, prefixValue)
        }

        // If the value against the prefix is a map, then we store the sub-keys in that map.
        if (!(prefixValue instanceof Map)) {
            return
        }

        Map nestedMap = (Map)prefixValue
        if (nestedIndex < nestedKey.length() - 1) {
            String remainderOfKey = nestedKey.substring(nestedIndex + 1, nestedKey.length())
            nestedMap.put(remainderOfKey, requestMap.get(key))
            if (!(nestedMap instanceof PathKeyMap) && remainderOfKey.indexOf(pathDelimiter) >-1) {
                processNestedKeys(requestMap, remainderOfKey, remainderOfKey, (Map)nestedMap)
            }
        }
    }
}
