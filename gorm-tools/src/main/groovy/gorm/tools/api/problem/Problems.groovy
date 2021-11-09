/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.problem

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.api.result.Result

@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class Problems implements Result {

    final Boolean ok = false
    Integer status = 400

    @Delegate List<Problem> problems

    Problems(boolean isSynchronized = true){
        problems = ( isSynchronized ? Collections.synchronizedList([]) : [] ) as List<Problem>
    }

    static Problems OK(){
        new Problems()
    }

    @Override //changes default list delegate so we can add ok
    boolean add(Problem prob){
        problems << prob
    }

    /**
     * addAll from the mergee.resultList
     * @param mergee what to merge into this one
     */
    void merge(Problems mergee){
        problems.addAll(mergee.problems)
    }

}
