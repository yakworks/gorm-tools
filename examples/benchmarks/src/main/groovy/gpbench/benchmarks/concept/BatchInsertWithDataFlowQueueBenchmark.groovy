package gpbench.benchmarks.concept

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.PoisonPill

import gorm.tools.transaction.WithTrx
import gpbench.benchmarks.BaseBatchInsertBenchmark
import gpbench.model.basic.CityBasic
import gpbench.repo.CityBasicRepo
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional

import static groovyx.gpars.dataflow.Dataflow.operator

/**
 * Runs batch inserts in parallel using gparse dataflow queue.
 */
@GrailsCompileStatic
class BatchInsertWithDataFlowQueueBenchmark extends BaseBatchInsertBenchmark implements WithTrx{

    CityBasicRepo cityRepo

    BatchInsertWithDataFlowQueueBenchmark(boolean databinding) { super(databinding) }

    BatchInsertWithDataFlowQueueBenchmark(String bindingMethod = 'grails', boolean validate = true) {
        super(CityBasic, bindingMethod, validate)
    }

    @Override
    def execute() {
        insert(cities, cityRepo)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void insert(List<List<Map>> batchList, CityBasicRepo repo) {
        DataflowQueue queue = new DataflowQueue()

        //setup an operator
        def op1 = operator(inputs: [queue], outputs: [], maxForks: poolSize) { List<Map> batch ->
            insertBatch(batch, repo)
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
    @CompileDynamic
    void insertBatch(List<Map> batch, CityBasicRepo repo) {
        for (Map record : batch) {
            try {
                //String dataBinder = dataBinder == 'copy' ? 'bindFast' : 'grailsWeb'
                repo.create(record)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        flushAndClear(transactionStatus)
    }

}
