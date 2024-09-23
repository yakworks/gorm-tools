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
    List<String> includes
    String includesKey

    static IncludesProps of(Map params){
        var incProps = new IncludesProps()
        if(params.containsKey('includes')) {
            incProps.includes = (params['includes'] as String).tokenize(',')*.trim()
        } else if(params.containsKey('includesKey')){
            incProps.includesKey = params['includesKey'] as String
        }
        return incProps
    }

    /**
     * Default keys for the includesKey
     */
    enum Keys {
        qSearch, get, picklist, list, stamp, bulk, bulkError
    }
}
