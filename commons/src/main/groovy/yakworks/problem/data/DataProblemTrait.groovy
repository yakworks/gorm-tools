/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.data

import groovy.transform.CompileStatic

import yakworks.api.ResultUtils
import yakworks.problem.ProblemException
import yakworks.problem.ProblemTrait

/**
 * Trait implementation for the Problem that has setters and builders
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait DataProblemTrait<E extends DataProblemTrait> extends ProblemTrait<E> {

    /**
     * convienience alias for payload so its clearer in the code
     * This is the entity that has problems, can be the id or the object
     */
    Object getEntity(){ return getPayload() }

    /**
     * builder method for entity that will add common args
     * such as name, id and stamp to the MsgKey
     */
    E entity(Object v) {
        if(v != null) {
            this.payload = v
            ResultUtils.addCommonArgs(args.asMap(), v)
        }
        return (E)this;
    }

    // overrides payload to call entity
    @Override
    E payload(Object v) {
        entity(v)
    }

    @Override
    ProblemException toException(){
        return getCause() ? new DataProblemException(getCause()).problem(this) : new DataProblemException().problem(this)
    }

}
