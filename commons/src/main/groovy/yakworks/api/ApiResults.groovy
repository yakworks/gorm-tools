/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

import yakworks.problem.Problem

/**
 * A Parent Result that has a list of Result(s).
 * The data in this case is a List of result/problem instances
 */
@CompileStatic
class ApiResults implements ResultTrait<ApiResults>, Serializable {
    Boolean ok = true
    ApiStatus status = HttpStatus.MULTI_STATUS

    //internal rep
    @Delegate List<Result> results

    //override so payload is the list of results
    @Override Object getPayload() { return results; }
    @Override void setPayload(Object v){ }

    /**
     * New result
     * @param isSynchronized defaults to true to create the data list as synchronizedList
     */
    ApiResults(boolean isSynchronized = true){
        results = ( isSynchronized ? Collections.synchronizedList([]) : [] ) as List<Result>
    }

    static ApiResults create(boolean isSynchronized = true){ new ApiResults(isSynchronized) }
    static ApiResults OK(){ new ApiResults() }


    @Override //changes default list delegate so we can add ok
    boolean add(Result result){
        if(!result.ok) ok = false
        results << result
    }

    /**
     * if resultToMerge is ApiResults then add all from its resultList
     * else just call add to list
     */
    void merge(Result resultToMerge){
        if(!resultToMerge.ok) ok = false
        if(resultToMerge instanceof ApiResults){
            results.addAll(resultToMerge.results as List<Result>)
        } else {
            results << resultToMerge
        }

    }

    /**
     * returns the problems
     */
    List<Problem> getProblems(){
        //only look if this is not ok as it should never have problems if ok=true
        if(this.ok){
            [] as List<Problem>
        } else {
            results.findAll{ it instanceof Problem } as List<Problem>
        }
    }

    /**
     * returns the successful results
     */
    List<Result> getOkResults(){
        results.findAll{ it.ok } as List<Result>
    }

    //Add these temporarily to be compatible with old Results
    List<Problem> getFailed(){
        getProblems()
    }
    List<Result> getSuccess(){
        getOkResults()
    }

}
