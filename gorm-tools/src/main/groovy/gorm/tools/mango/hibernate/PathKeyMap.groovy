/*
* Copyright 2004-2005 original authors
* SPDX-License-Identifier: Apache-2.0
*/
package gorm.tools.mango.hibernate

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

/**
 * A redo of the  GrailsParameterMap, primary to remove the need for HttpServletRequest.
 * Allows a flattened map of path keys such that
 * foo.bar.id:1, foo.amount:10 would end up as [foo: [bar: [id: 1]], amount:10]
 * Useful for CSV reading too.
 */
@SuppressWarnings(["ExplicitCallToEqualsMethod"])
@CompileStatic
class PathKeyMap<String, Object> implements Map<String, Object>, Cloneable  {

    Map<String, Object> wrappedMap

    String pathDelimiter = '.'

    boolean initialized = false

    /**
     * Populates the PathKeyMap with supplied map.
     *
     * @param values The values to populate with
     */
    PathKeyMap(Map<String, Object> sourceMap) {
        wrappedMap = sourceMap?:[:]
    }

    static <K, V> PathKeyMap<K,V> of(Map<K,V> sourceMap){
        return new PathKeyMap(sourceMap)
    }

    static <K, V> PathKeyMap<K,V> of(Map<K,V> sourceMap, String pathDelimiter ){
        def pkm = new PathKeyMap(sourceMap)
        pkm.pathDelimiter = pathDelimiter
        return pkm
    }

    static <K, V> PathKeyMap<K,V> create(Map<K,V> sourceMap){
        def pkm = new PathKeyMap(sourceMap)
        return pkm.init()
    }

    PathKeyMap<K,V> pathDelimiter(String v){
        this.pathDelimiter = v
        return this
    }


    //need this, or else, groovy metaclass would call 'get' method of this class, resulting in StackOverflow error
    //See MetaClassImpl.getProperty
    protected Map<K,V> getWrappedMap() {
        return this.wrappedMap //direct field access
    }

    PathKeyMap cloneMap() {
        if (wrappedMap.isEmpty()) {
            return PathKeyMap.of([:], pathDelimiter)
        } else {
            // Map clonedMap = Maps.clone(wrappedMap)
            Map clonedMap = new LinkedHashMap(wrappedMap)
            // deep clone nested entries
            clonedMap.keySet().each { k ->
                def val = clonedMap[k]
                //clone the nested values that are pathKeyMaps
                if (val instanceof PathKeyMap) {
                    clonedMap[k] = (val as PathKeyMap).cloneMap()
                }
                // if its a list of PathKeyMaps then iterate over and clone those too
                else if(val && val instanceof Collection && ((Collection)val)[0] instanceof PathKeyMap) {
                    clonedMap[k] = val.collect{
                        (it as PathKeyMap).cloneMap()
                    } as Collection<PathKeyMap>
                }
            }

            return PathKeyMap.of(clonedMap, pathDelimiter)
        }
    }

    @Override
    public Object clone() {
        cloneMap()
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
            return wrappedMap.subMap((Collection)key)
        }

        return returnValue
    }

    @Override
    Object put(Object key, Object value) {
        if (value instanceof CharSequence) value = value.toString()
        if (key instanceof CharSequence) key = key.toString()
        Object returnValue =  wrappedMap.put((K)key, (V)value)
        if (key instanceof String) {
            String keyString = (String)key
            if (keyString.indexOf(pathDelimiter) > -1) {
                processNestedKeys(this, keyString, wrappedMap)
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
        return get("id")
    }

    /**
     * Process the nested keys
     */
    PathKeyMap init() {
        if(initialized) return this

        for (Object keyObject : wrappedMap.keySet().collect{it}) {
            String key = (String)keyObject
            processNestedKeys(wrappedMap, key, wrappedMap)
            if(key.contains(pathDelimiter)) wrappedMap.remove(keyObject)
        }
        initialized = true
        return this
    }

    /*
     * Builds up a multi dimensional hash structure from the parameters so that nested keys such as
     * "book.author.name" can be addressed like params['author'].name
     *
     * This also allows data binding to occur for only a subset of the properties in the parameter map.
     */
    private void processNestedKeys(Map requestMap, String key, Map nestedLevel) {
        final int nestedIndex = key.indexOf(pathDelimiter)

        if (nestedIndex == -1) {
            def val = requestMap.get(key)
            if(val instanceof PathKeyMap) val.init()
            if(val && val instanceof Collection && ((Collection)val)[0] instanceof PathKeyMap) {
                val.each{ ((PathKeyMap)it).init()}
            }
            return
        }

        // We have at least one sub-key, so extract the first element
        // of the nested key as the prfix. In other words, if we have
        // 'nestedKey' == "a.b.c", the prefix is "a".
        String nestedPrefix = key.substring(0, nestedIndex)

        // Let's see if we already have a value in the current map for the prefix.
        Object prefixValue = nestedLevel.get(nestedPrefix)
        if (prefixValue == null) {
            // No value. So, since there is at least one sub-key,
            // we create a sub-map for this prefix.

            prefixValue = PathKeyMap.of([:], pathDelimiter)
            nestedLevel.put(nestedPrefix, prefixValue)
        }

        // If the value against the prefix is a map, then we store the sub-keys in that map.
        if (!(prefixValue instanceof Map)) {
            return
        }

        Map nestedMap = (Map)prefixValue
        if (nestedIndex < key.length() - 1) {
            String remainderOfKey = key.substring(nestedIndex + 1, key.length())
            nestedMap.put(remainderOfKey, requestMap.get(key))
            if (!(nestedMap instanceof PathKeyMap) && remainderOfKey.indexOf(pathDelimiter) >-1) {
                processNestedKeys(requestMap, remainderOfKey, (Map)nestedMap)
            }
        }
    }

    @Override
    boolean equals(Object that) {
        wrappedMap.equals(that)
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash();
        for (Map.Entry<String, Object> entry: wrappedMap.entrySet()) {
            hashCode = HashCodeHelper.updateHash(hashCode, entry);
        }
        return hashCode;
    }

    /**
     * Helper method for obtaining a list of values from parameter
     * @param name The name of the parameter
     * @return A list of values
     */
    List getList(String name) {
        Object paramValues = get(name);
        if (paramValues == null) {
            return Collections.emptyList();
        }
        if (paramValues.getClass().isArray()) {
            return Arrays.asList((Object[])paramValues);
        }
        if (paramValues instanceof Collection) {
            return new ArrayList((Collection)paramValues);
        }
        return Collections.singletonList(paramValues);
    }

    List list(String name) {
        return getList(name);
    }

    @Override
    int size() {
        return wrappedMap.size();
    }

    @Override
    boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    @Override
    boolean containsKey(Object k) {
        return wrappedMap.containsKey(k);
    }

    @Override
    boolean containsValue(Object v) {
        return wrappedMap.containsValue(v);
    }

    @Override
    void clear() {
        wrappedMap.clear();
    }

    @Override
    Set keySet() {
        return wrappedMap.keySet();
    }

    @Override
    Collection values() {
        return wrappedMap.values();
    }

    @Override
    Set entrySet() {
        return wrappedMap.entrySet();
    }

    @Override
    String toString() {
        return this.toMapString()
    }
}
