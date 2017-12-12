package gorm.tools.async

import groovy.transform.CompileStatic

@CompileStatic
interface AsyncBatchProcess {

    void eachParallel(Map args, List<List<Map>> batchList, Closure clos)
    //void eachParallel(List<List<Map>> batchList, Closure clos)
    /**
     * Uses collate to break list into batches and then runs with eachParralel
     */
    void eachCollate(List<Map> batchList, Map args, Closure clos)

    void withTransaction(List<Map> batch,  Map args, Closure clos)

}
