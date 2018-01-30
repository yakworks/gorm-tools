package gpbench.benchmarks.concept

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.EntityValidationException
import gpbench.basic.CityBasicRepo
import gpbench.benchmarks.BaseBatchInsertBenchmark
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.springframework.dao.DataAccessException

/**
 * Runs batch inserts with exception.
 */
@CompileStatic
class ExceptionHandlingBenchmark extends BaseBatchInsertBenchmark {

    CityBasicRepo cityRepo
    Class exceptionToThrow
    Class exceptionToCatch


    ExceptionHandlingBenchmark(boolean databinding, Class exceptionToThrow = EntityValidationException, Class exceptionToCatch=DataAccessException) {
        super(databinding)
        this.exceptionToThrow = exceptionToThrow
        this.exceptionToCatch = exceptionToCatch
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
                throw exceptionToThrow.newInstance("test")
            } catch (exceptionToCatch) {
                repo.create(record)
            }
        }
    }

    @Override
    String getDescription() {
        return "ExceptionHandlingBenchmark without gpars: databinding=${useDatabinding}"
    }
}
