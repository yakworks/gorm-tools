package gpbench.benchmarks.concept

import groovy.transform.CompileStatic

import org.springframework.dao.DataAccessException

import gorm.tools.problem.ValidationProblem
import gpbench.benchmarks.BaseBatchInsertBenchmark
import gpbench.repo.CityBasicRepo
import grails.gorm.transactions.Transactional

/**
 * Runs batch inserts with exception.
 */
@CompileStatic
class ExceptionHandlingBenchmark extends BaseBatchInsertBenchmark {

    CityBasicRepo cityRepo
    Class exceptionToThrow
    Class exceptionToCatch


    ExceptionHandlingBenchmark(boolean databinding, Class exceptionToThrow = ValidationProblem, Class exceptionToCatch=DataAccessException) {
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
            repo.flushAndClear()
        }
    }

    @Transactional
    void insertBatch(List<Map> batch, CityBasicRepo repo) {
        for (Map record : batch) {
            try {
                throw exceptionToThrow.newInstance("test") as Throwable
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
