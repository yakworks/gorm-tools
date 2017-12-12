package gpbench.benchmarks

import groovy.transform.CompileStatic

/**
 * Created by sudhir on 27/10/17.
 */
@CompileStatic
abstract class AbstractBenchmark {

	BigDecimal timeTaken

	void setup() {}
	void cleanup(){}

	protected run() {
		setup()
		Long start = System.currentTimeMillis()
		execute()
		Long end = System.currentTimeMillis()
		timeTaken = ((end - start) / 1000)
		cleanup()
	}

	protected abstract execute()

	String getDescription() {
		return getClass().simpleName
	}
}
