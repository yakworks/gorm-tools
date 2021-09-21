/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy= SimpleStrategy, prefix="")
@TupleConstructor
@CompileStatic
class ParallelEach<T>  {

    Collection<T> collection

    ParallelTools parallelTools


    /**
     * Iterates over a collection/object with the <i>each()</i> method using an
     * asynchronous variant, if enabled of the supplied closure to evaluate each collection's element.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    // def each(Closure cl) {
    //     parallelTools.eachParallel(this, collection, cl)
    // }

    def eachSlice(Closure cl) {

    }



}
