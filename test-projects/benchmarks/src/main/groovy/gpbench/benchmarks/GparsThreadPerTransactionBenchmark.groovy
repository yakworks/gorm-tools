package gpbench.benchmarks

import gpbench.City
import gpbench.CityDao
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
		assert City.count() == 0
		insert(cities, cityDao)
		assert City.count() == 115000
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void insert(List<Map> batch, CityDao dao) {
		GParsPool.withPool(poolSize) {
			batch.eachWithIndexParallel { Map record, int index ->
				insert(record, dao)
			}
		}
	}

	@Transactional
	void insert(Map record, CityDao dao) {
		try {
			if (useDatabinding) dao.create(record)
			else dao.insertWithSetter(record)
		} catch (Exception e) {
			e.printStackTrace()
		}

	}

	@Override
	String getDescription() {
		return "Gpars one thread per transaction: databinding=${useDatabinding}"
	}
}
