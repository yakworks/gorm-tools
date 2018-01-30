package gpbench.benchmarks.legacy

import gorm.tools.repository.RepoUtil
import gpbench.basic.CityBasicRepo
import gpbench.basic.CityBasic
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**Inserts all records in a single big transaction.
 */
@CompileStatic
class OneBigTransactionBenchmark extends BaseBenchmark {

    OneBigTransactionBenchmark(boolean databinding) {
        super(databinding)
    }

    @Override
    def execute() {
        assert CityBasic.count() == 0
        insert(cities, cityRepo)
        assert CityBasic.count() == 115000
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    @Transactional
    void insert(List<Map> batch, CityBasicRepo repo) {
        batch.eachWithIndex { Map record, int index ->
            insert(record, repo)
            if (index % batchSize == 0) RepoUtil.flushAndClear()
        }
    }

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
        return "All records in one big single transaction: databinding=${useDatabinding}"
    }
}
