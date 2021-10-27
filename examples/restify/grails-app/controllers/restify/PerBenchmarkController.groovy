package restify

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class PerBenchmarkController {
    BulkPerfBenchmarkService bulkPerfBenchmarkService

    /**
     * Benchmark just binding & create without json serialisation etc
     */
    def insert() {
        Long start = System.currentTimeMillis()
        bulkPerfBenchmarkService.insert(10 * 1000)
        Long end = System.currentTimeMillis()
        render text: "Took : ${end - start} Millis"
    }
}
