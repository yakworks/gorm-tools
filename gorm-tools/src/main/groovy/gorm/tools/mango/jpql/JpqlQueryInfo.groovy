/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import groovy.transform.CompileStatic


@CompileStatic
@SuppressWarnings("rawtypes")
public class JpqlQueryInfo {

    String query
    List parameters

    public JpqlQueryInfo(String query, List parameters) {
        this.query = query
        this.parameters = parameters
    }

    public String getQuery() {
        return query
    }

    public List getParameters() {
        return parameters
    }
    public Map getParamMap() {
        Map pmap = [:]
        parameters.eachWithIndex{ v, i ->
            pmap["p${i+1}"] = v
        }
        return pmap
    }
}
