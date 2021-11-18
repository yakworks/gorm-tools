/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus

/**
 * fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class EntityNotFoundProblem extends AbstractDataAccessProblem<EntityNotFoundProblem> {
    String defaultCode = 'error.notFound'
    ApiStatus status = HttpStatus.NOT_FOUND

    // the look up key, mostly will be the id, but could be code or map with sourceId combos
    Serializable key

    // the name of the entity that was being looked up
    String name

    protected EntityNotFoundProblem() {
        super('error.notFound')
    }

    EntityNotFoundProblem name(String nm){
        this.name = nm
        putArgIfAbsent('name',nm)
        return this
    }

    EntityNotFoundProblem lookupKey(Serializable k){
        this.key = (k instanceof Map ? k : [id: k]) as Serializable
        putArgIfAbsent('key', key)
        return this
    }

    static EntityNotFoundProblem of(Serializable key, String entityName) {
        def p = new EntityNotFoundProblem().lookupKey(key).name(entityName)
        p.detail("$entityName lookup failed using key ${p.key}")
    }



    //Override it for performance improvement, because filling in the stack trace is quit expensive
    @SuppressWarnings(['SynchronizedMethod'])
    @Override
    synchronized Throwable fillInStackTrace() { return this }
}
