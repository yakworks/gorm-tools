/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.transform.CompileStatic

/**
 * The props passed in from params.
 */
@CompileStatic
class IncludesProps {
    /** specified list of fields, ex [id, num, name, etc..] */
    List<String> includes
    /** the key name to find includes on the api config */
    String includesKey
    /** The fallback keys */
    List<String> fallbackKeys = []

    static IncludesProps of(Map params){
        var incProps = new IncludesProps()
        if(params.containsKey('includes')) {
            incProps.includes = (params['includes'] as String).tokenize(',')*.trim()
        } else if(params.containsKey('includesKey')){
            incProps.includesKey = params['includesKey'] as String
        }
        return incProps
    }

    IncludesProps fallbackKey(String val){
        this.fallbackKeys << val
        return this
    }

    IncludesProps fallbackKeys(List val){
        this.fallbackKeys = val*.toString()
        return this
    }

    List<String> getIncludesKeys(){
        List<String> keyList = []
        if(includesKey){
            keyList << includesKey
        }
        keyList.addAll(fallbackKeys)
        keyList
    }

    List<String> findIncludes(Class clazz){
        return IncludesConfig.bean().findIncludes(clazz, this)
    }

    /**
     * Default keys for the includesKey
     */
    enum Keys {
        qSearch, get, picklist, list, stamp, bulk, bulkError
    }
}
