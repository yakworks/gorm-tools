package restify

import org.grails.core.util.StopWatch

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class PerBenchmarkController {
    BulkPerfBenchmarkService bulkPerfBenchmarkService

    /**
     * Benchmark just binding & create without json serialisation etc
     */
    def insert() {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        bulkPerfBenchmarkService.insert(10 * 1000)
        stopWatch.stop()

        render text:stopWatch.shortSummary()
    }
}
