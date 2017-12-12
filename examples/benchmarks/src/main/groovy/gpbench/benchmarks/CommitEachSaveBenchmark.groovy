package gpbench.benchmarks

import gorm.tools.dao.DaoUtil
import gpbench.City
import gpbench.CityDao
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 *  Commit each record in individual transaction
 */
@CompileStatic
class CommitEachSaveBenchmark extends BaseBenchmark {

	CommitEachSaveBenchmark(boolean databinding) {
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
		batch.eachWithIndex { Map record, int index ->
			insert(record, dao)
			if (index % batchSize == 0) DaoUtil.flushAndClear()
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
		return "Commit each save: databinding=${useDatabinding}"
	}
}
