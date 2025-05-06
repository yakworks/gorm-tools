/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.hibernate

import groovy.transform.CompileStatic

import yakworks.commons.map.LazyPathKeyMap
import yakworks.commons.model.TotalCount

/**
 * Simple Paged List that delegates to a list and has the totalCount.
 * Example would be for results from server where  this list hold 10 results with totalCount = 100
 * Intentionally simple with no tracking for what page its on etc..
 */
@CompileStatic
class PathKeyMapPagedList implements TotalCount {
    @Delegate List<LazyPathKeyMap> results

    PathKeyMapPagedList(List<LazyPathKeyMap> results, int tot) {
        this.results = results
        setTotalCount(tot)
    }

    // @Override
    // LazyPathKeyMap get(int index){
    //     LazyPathKeyMap item = results.get(index)
    //     return item
    // }
}
