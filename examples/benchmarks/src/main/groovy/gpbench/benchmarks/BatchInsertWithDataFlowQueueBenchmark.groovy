package gpbench.benchmarks

import gorm.tools.dao.DaoUtil
import gpbench.City
import gpbench.CityDao
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.PoisonPill

import static groovyx.gpars.dataflow.Dataflow.operator

/**
 * Runs batch inserts in parallel using gparse dataflow queue.
 */
@CompileStatic
class BatchInsertWithDataFlowQueueBenchmark extends BaseBatchInsertBenchmark {

    CityDao cityDao
    BatchInsertWithDataFlowQueueBenchmark(boolean databinding) { super(databinding) }

    BatchInsertWithDataFlowQueueBenchmark(String bindingMethod = 'grails', boolean validate = true) {
        super(City, bindingMethod,validate)
    }

    @Override
    def execute() {
        insert(cities, cityDao)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void insert(List<List<Map>> batchList, CityDao dao) {
        DataflowQueue queue = new DataflowQueue()

        //setup an operator
        def op1 = operator(inputs: [queue], outputs: [], maxForks:poolSize) {List<Map> batch ->
            insertBatch(batch, dao)
        }

        final int MAX_QUEUE_SIZE = 10
        batchList.each { List<Map> batch ->
            while (queue.length() > MAX_QUEUE_SIZE) {
                Thread.yield()
            }

            queue << batch
        }

        //give operator a poision pill, so it will stop after finishing whatever batches are still in queue (cold shutdown).
        queue << PoisonPill.instance

        op1.join() //wait for operator to finish

    }

    @Transactional
    void insertBatch(List<Map> batch, CityDao dao) {
        for (Map record : batch) {
            try {
                //String dataBinder = dataBinder == 'copy' ? 'bindFast' : 'grailsWeb'
                dao.create(record)
            }catch (Exception e) {
                e.printStackTrace()
            }
        }

        DaoUtil.flushAndClear()
    }

}
