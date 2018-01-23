package gpbench.benchmarks.legacy

import gorm.tools.repository.RepoUtil
import gpbench.City
import gpbench.CityRepo
import gpbench.benchmarks.BaseBatchInsertBenchmark
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

/**
 * Runs batch inserts but without gpars.
 */
@CompileStatic
class SimpleBatchInsertBenchmark extends BaseBatchInsertBenchmark {

    CityRepo cityRepo

    SimpleBatchInsertBenchmark(boolean databinding) {
        super(databinding)
    }

    @Override
    def execute() {
        //assert City.count() == 0
        insert(cities, cityRepo)
        //assert City.count() == 115000
    }

    void insert(List<List<Map>> batchList, CityRepo repo) {
        for (List<Map> batch : batchList) {
            insertBatch(batch, repo)
            RepoUtil.flushAndClear()
        }
    }

    @Transactional
    void insertBatch(List<Map> batch, CityRepo repo) {
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
