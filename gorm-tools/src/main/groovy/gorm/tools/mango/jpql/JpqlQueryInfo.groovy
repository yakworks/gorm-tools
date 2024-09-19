/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import groovy.transform.CompileStatic

/**
 * The result of the JpqlQueryBuilder
 */
@CompileStatic
@SuppressWarnings("rawtypes")
class JpqlQueryInfo {

    //full query
    String query
    //where clause
    String where

    List parameters

    JpqlQueryInfo(String query, List parameters) {
        this.query = query
        this.parameters = parameters
    }

    String getQuery() {
        return query
    }

    List getParameters() {
        return parameters
    }
    Map getParamMap() {
        Map pmap = [:]
        parameters.eachWithIndex{ v, i ->
            pmap["p${i+1}"] = v
        }
        return pmap
    }
}
