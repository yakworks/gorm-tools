/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import yakworks.json.groovy.HttpJsonParserTrait

/**
 * methods to parse the request body json
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait RequestJsonSupport extends HttpJsonParserTrait{

    abstract HttpServletRequest getRequest()

    /**
     * calls parseJson with the internal request
     */
    public <T> T parseJson(Class<T> clazz) {
        return parseJson(getRequest() , clazz)
    }

    /**
     * parse request body content as a Map
     */
    Map bodyAsMap() {
        return parseJson(Map)
    }

    /**
     * parse request body content as a List
     */
    List bodyAsList() {
        return parseJson(List)
    }

    /**
     * Deprecated, just calls parseJson(getRequest())
     * TODO but maybe keep this one and have it be a merger the json body with params
     */
    Map getDataMap() {
        return bodyAsMap()
    }


}
