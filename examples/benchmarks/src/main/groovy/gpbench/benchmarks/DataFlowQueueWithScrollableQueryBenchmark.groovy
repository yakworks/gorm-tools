package gpbench.benchmarks

import gorm.tools.dao.DaoUtil
import gorm.tools.jdbc.GrailsParameterMapRowMapper
import gorm.tools.jdbc.ScrollableQuery
import gpbench.City
import gpbench.CityDao
import gpbench.helpers.BenchmarkHelper
import gpbench.helpers.CsvReader
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.PoisonPill
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

import javax.sql.DataSource

import static groovyx.gpars.dataflow.Dataflow.operator

/**
 * Runs batch inserts in parallel using gparse dataflow queue
 * while records are simalutanelously loaded from another table using scrollable query. CED Way.
 */
@CompileStatic
class DataFlowQueueWithScrollableQueryBenchmark extends AbstractBenchmark {
	int poolSize
	int batchSize

	BenchmarkHelper benchmarkHelper
	JdbcTemplate jdbcTemplate
	DataSource dataSource
	CityDao cityDao
	CsvReader csvReader

	void setup() {
		Long start = System.currentTimeMillis()
		//create temp table to hold data and insert records in.
		benchmarkHelper.executeSqlScript("test-tables.sql")
		Long count = jdbcTemplate.queryForObject("select count(*) FROM city_tmp", Long)
		if(count > 0) return

		String query = "insert into city_tmp (name, latitude, longitude, shortCode, `country.id`, `region.id`) values (?, ?, ?, ?, ?, ?)"
		Sql sql = new Sql(dataSource)
		List<Map> cityRecords = csvReader.read("City100k")
		cityRecords.each { Map m ->
			List params = [m.name, m.latitude as Float, m.longitude as Float, m.shortCode, m['country.id'] as Long, m['region.id'] as Long]
			sql.execute query, params
		}
		Long end = System.currentTimeMillis()
		println "${((end - start) / 1000)}s to load city temp table"
	}

	@Override
	def execute() {
		assert City.count() == 0

		RowMapper<Map> mapper = new GrailsParameterMapRowMapper()
		ScrollableQuery query = new ScrollableQuery(mapper, dataSource,  batchSize)
		insert(query)
		assert City.count() == 115000
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void insert(ScrollableQuery query) {
		String q = "select * from city_tmp"
		DataflowQueue queue = new DataflowQueue()

		//setup an operator
		def op1 = operator(inputs: [queue], outputs: [], maxForks:poolSize) {List<Map> batch ->
			insertBatch(batch)
		}

		final int MAX_QUEUE_SIZE = 10
		query.eachBatch(q, batchSize) { List<Map> batch ->
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
	void insertBatch(List<Map> batch) {
		for (Map record : batch) {
			try {
				cityDao.insertWithSetter(record)
			}catch (Exception e) {
				e.printStackTrace()
			}
		}

		DaoUtil.flushAndClear()
	}

	@Override
	String getDescription() {
		return "DataFlawQueueWithScrollableQuery: databinding: false"
	}
}
