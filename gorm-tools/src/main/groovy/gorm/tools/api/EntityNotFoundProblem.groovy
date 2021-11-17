/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.i18n.MsgKey

/**
 * fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityNotFoundProblem extends DataAccessProblem<EntityNotFoundProblem> {
    public static String DEFAULT_CODE = 'error.notFound'

    String defaultCode = DEFAULT_CODE
    ApiStatus status = HttpStatus.NOT_FOUND

    // the look up key, mostly will be the id, but could be code or map with sourceId combos
    Serializable identifier

    protected EntityNotFoundProblem() {
        super(null)
    }

    EntityNotFoundProblem(Serializable data, String entityName) {
        super(DEFAULT_CODE)
        identifier = data
        Map dataMap
        if(data instanceof Number) dataMap = [id: data]
        if(data instanceof Map) dataMap = data
        // if(data instanceof Number)
        this.msg = MsgKey.of(DEFAULT_CODE, [name: entityName, id: data])
        this.detail = "Lookup failed for $entityName using data $dataMap"
    }

    @Override
    EntityNotFoundProblem getCause() {
        // cast is safe, since the only way to set this is our constructor
        return (EntityNotFoundProblem) super.getCause()
    }

    static EntityNotFoundProblem of(Serializable data, String entityName) {
        new EntityNotFoundProblem(data, entityName)
    }



    //Override it for performance improvement, because filling in the stack trace is quit expensive
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() { return this }
}
