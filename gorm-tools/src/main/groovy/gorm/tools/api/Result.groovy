/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.api.problem.Problem

/**
 * Used as a result object in api
 *
 * @author Joshua Burnett (@basejump)
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@ToString @EqualsAndHashCode
@CompileStatic
class Result {
    boolean ok = true

    int status

    /**
     * the entity that was created
     */
    Object entityObject

    /**
     * the entity fields for what was created or updated
     * if it errored then this will be null
     */
    Map entityData

    /**
     * the data the was submitted to process.
     * will either be one of the items in the list that was sent or the list slice that failed on flush/commit
     */
    Object requestData

    /**
     * On error this is the processed error based on exception type
     */
    Problem problem

    // Result addTo(BulkableResults results){
    //     results.add(this)
    //     this
    // }

    static Result of(Problem error){
        new Result(ok: false, problem: error, status: error.status)

    }

    static Result of(Problem error, Object requestData){
        new Result(ok: false, problem: error, requestData: requestData, status: error.status)

    }

    static Result of(Object entity){
        new Result(entityObject: entity)
    }

    static Result of(Object entity, int statusId){
        of(entity).status(statusId)
    }

}
