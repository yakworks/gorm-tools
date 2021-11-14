/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api

import groovy.transform.CompileStatic

import yakworks.api.problem.ProblemBase

/**
 * A Parent Result that has a list of Result(s).
 * The data in this case is a List of result/problem instances
 */
@CompileStatic
class ApiResults extends AbstractResult<ApiResults, List<Result>> implements Serializable {
    Boolean ok = true
    ApiStatus status = HttpStatus.MULTI_STATUS

    @Delegate List<Result> data

    /**
     * New result
     * @param isSynchronized defaults to true to create the data list as synchronizedList
     */
    ApiResults(boolean isSynchronized = true){
        data = ( isSynchronized ? Collections.synchronizedList([]) : [] ) as List<Result>
    }

    static ApiResults OK(){ new ApiResults() }

    @Override //changes default list delegate so we can add ok
    boolean add(Result result){
        if(!result.ok) ok = false
        data << result
    }

    /**
     * if resultToMerge is ApiResults then add all from its resultList
     * else just call add to list
     */
    void merge(Result resultToMerge){
        if(!resultToMerge.ok) ok = false
        if(resultToMerge instanceof ApiResults){
            data.addAll(resultToMerge.data as List<Result>)
        } else {
            data << resultToMerge
        }

    }

    /**
     * returns the problems
     */
    List<ProblemBase> getProblems(){
        //only look if this is not ok as it should never have problems if ok=true
        if(this.ok){
            [] as List<ProblemBase>
        } else {
            data.find{ it instanceof ProblemBase } as List<ProblemBase>
        }
    }

    /**
     * returns the successful results
     */
    List<Result> getOkResults(){
        data.find{ it.ok } as List<Result>
    }

    //Add these temporarily to be compatible with old Results
    List<ProblemBase> getFailed(){
        getProblems()
    }
    List<Result> getSuccess(){
        getOkResults()
    }

}
