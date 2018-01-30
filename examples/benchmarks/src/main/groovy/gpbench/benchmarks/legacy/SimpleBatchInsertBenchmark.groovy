package gpbench.benchmarks.legacy

import gorm.tools.repository.RepoUtil
import gpbench.basic.CityBasicRepo
import gpbench.benchmarks.BaseBatchInsertBenchmark
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts but without gpars.
 */
@CompileStatic
class SimpleBatchInsertBenchmark extends BaseBatchInsertBenchmark {

    CityBasicRepo cityRepo

    SimpleBatchInsertBenchmark(boolean databinding) {
        super(databinding)
    }

    @Override
    def execute() {
        //assert CityBasic.count() == 0
        insert(cities, cityRepo)
        //assert CityBasic.count() == 115000
    }

    void insert(List<List<Map>> batchList, CityBasicRepo repo) {
        for (List<Map> batch : batchList) {
            insertBatch(batch, repo)
            RepoUtil.flushAndClear()
        }
    }

    @Transactional
    void insertBatch(List<Map> batch, CityBasicRepo repo) {
        for (Map record : batch) {
            try {
                repo.create(record)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    @Override
    String getDescription() {
        return "SimpleBatchInsert without gpars: databinding=${useDatabinding}"
    }
}
