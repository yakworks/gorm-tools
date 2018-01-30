package gpbench.benchmarks.legacy

import gpbench.basic.CityBasic
import gpbench.basic.CityBasicRepo
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool

//import groovyx.gpars.GParsPool

/**
 * Runs inserts in parallel using gparse with one thread per transaction.
 */
@CompileStatic
class GparsThreadPerTransactionBenchmark extends BaseBenchmark {

    GparsThreadPerTransactionBenchmark(boolean databinding) {
        super(databinding)
    }

    @Override
    def execute() {
        assert CityBasic.count() == 0
        insert(cities, cityRepo)
        assert CityBasic.count() == 115000
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void insert(List<Map> batch, CityBasicRepo repo) {
        GParsPool.withPool(poolSize) {
            batch.eachWithIndexParallel { Map record, int index ->
                insert(record, repo)
            }
        }
    }

    @Transactional
    void insert(Map record, CityBasicRepo repo) {
        try {
            if (useDatabinding) repo.create(record)
            else repo.insertWithSetter(record)
        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    @Override
    String getDescription() {
        return "Gpars one thread per transaction: databinding=${useDatabinding}"
    }
}
