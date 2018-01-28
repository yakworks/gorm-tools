package gpbench

import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gpbench.benchmarks.*
import gpbench.benchmarks.legacy.SimpleBatchInsertBenchmark
import gpbench.benchmarks.read.ReadBenchmark
import gpbench.benchmarks.concept.BatchInsertWithDataFlowQueueBenchmark
import gpbench.benchmarks.concept.ExceptionHandlingBenchmark
import gpbench.benchmarks.concept.RxJavaBenchmark
import gpbench.benchmarks.update.GparsBaseLineUpdateBenchmark
import gpbench.benchmarks.update.RepoUpdateBenchmark
import gpbench.benchmarks.update.UpdateBenchmark
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.traits.BenchConfig
import org.springframework.dao.DataAccessException

class BenchmarkRunnerService implements BenchConfig {

    //@CompileStatic
    void runBenchMarks() {
        setup()

        //real benchmarks starts here
        println "\n- Running Benchmarks, loading ${loadIterations * 37230} items each run"

        if (System.getProperty("runSingleThreaded", "false").toBoolean()) {
            println "-- single threaded - no gpars"
            runBenchmark(new SimpleBatchInsertBenchmark(true))
        }

        //warmUpAndRun("### Exception handling", "runWithExceptions", binderType)

        // warmUpAndRun("### Gpars - fat props","runFat", binderType)

        warmUpAndRun("### Gpars - update benchmarks", "runUpdateBenchmarks", binderType)
        warmUpAndRun("### Gpars - Assign Properties, no grails databinding", "runBaselineCompare", binderType)

        warmUpAndRun("### Repo events - set audit fields", "runRepoEvents", binderType)

        if (auditTrailEnabled)
            warmUpAndRun("### Gpars - audit trail", "runWithAuditTrail", binderType)

        if (eventListenerCount)
            warmUpAndRun("### Gpars - with events in refreshable groovy script bean", "runWithEvents", binderType)

        // warmUpAndRun("### Gpars - fat props","runFast", binderType)

        // warmUpAndRun("### RXJava, Script executor, etc", "runOther", binderType)

//        warmUpAndRun("  - Performance problems go away without databinding on traits",
//            "runMultiCoreSlower", 'fast')

        warmUpAndRun("### Reading record", "runRead", binderType)

        warmUpAndRun("### Updating records", "runUpdate", binderType)

        //runMultiThreadsOther("## Misc sanity checks")

        System.exit(0)
    }

    void runBaselineCompare(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        logMessage "  - Grails Basic Baseline to measure against"
        runBenchmark(new GparsBaselineBenchmark(CityBaseline, bindingMethod))
        runBenchmark(new GparsBaselineBenchmark(City, bindingMethod))

        logMessage "  - Events disabled"
        City.repo.enableEvents = false
        runBenchmark(new GparsRepoBenchmark(City, bindingMethod))

        logMessage "  - Events enabled"
        City.repo.enableEvents = true
        runBenchmark(new GparsRepoBenchmark(City, bindingMethod))

        //runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic, bindingMethod))
        //logMessage "\n  - These should all run within about 5% of City and each other"
        //runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod))
    }

    void runUpdateBenchmarks(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaseLineUpdateBenchmark(CityBaseline))
        runBenchmark(new GparsBaseLineUpdateBenchmark(City))
        runBenchmark(new RepoUpdateBenchmark(CityBaseline))
        runBenchmark(new RepoUpdateBenchmark(City))
    }

    void runWithEvents(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaselineBenchmark(CityRefreshableBeanEvents, bindingMethod))
    }

    void runRepoEvents(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"

        logMessage "  - Spring Events disabled, invokes only method events"
        CityMethodEvents.repo.enableEvents = false
        runBenchmark(new GparsRepoBenchmark(CityMethodEvents, bindingMethod))

        logMessage "  - All Events enabled"
        runBenchmark(new GparsRepoBenchmark(CitySpringEvents, bindingMethod))
        runBenchmark(new GparsRepoBenchmark(CitySpringEventsRefreshable, bindingMethod))
    }

    void runWithAuditTrail(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod))
    }

    void runOther(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new RxJavaBenchmark(City, bindingMethod))
        runBenchmark(new GparsScriptEngineBenchmark(City, bindingMethod))
    }

    void runFat(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        logMessage "  - benefits of CompileStatic and 'fast' binding are more obvious with more fields"
        runBenchmark(new GparsFatBenchmark(CityFatDynamic, bindingMethod))
        runBenchmark(new GparsFatBenchmark(CityFat, bindingMethod))
    }

    void runMultiThreadsOther(String msg) {
        println "\n$msg"
        runBenchmark(new BatchInsertWithDataFlowQueueBenchmark('fast'))

        logMessage "  - using copy instead of binding and no validation, <10% faster"
        runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic, 'fast', false))

        println "\n - assign id inside domain with beforeValidate"
        //runBenchmark(new GparsBaselineBenchmark(CityIdGenAssigned))

        println "\n  - not much difference between static and dynamic method calls"
//		runBenchmark(new GparsRepoBenchmark(City,"setter"))
//		runBenchmark(new GparsRepoBenchmark(City,"fast"))
//
//		runBenchmark(new GparsRepoBenchmark(City,"bindWithSetters"))
//		runBenchmark(new GparsRepoBenchmark(City,"bindFast"))

        new City().attached

    }

    void runWithExceptions(String msg, String binding) {
        logMessage "\n$msg"
        println "-- single threaded without exception, just for comparison"
        runBenchmark(new SimpleBatchInsertBenchmark(true))
        println "-- Exceptions thrown - EntityValidationException, catched - EntityValidationException"
        runBenchmark(new ExceptionHandlingBenchmark(true, EntityValidationException, EntityValidationException))
        println "-- Exceptions thrown - EntityValidationException, catched - DataAccessException"
        runBenchmark(new ExceptionHandlingBenchmark(true, EntityValidationException, DataAccessException))
        println "-- Exceptions thrown - grails.validation.ValidationException, catched - grails.validation.ValidationException"
        runBenchmark(new ExceptionHandlingBenchmark(true, grails.validation.ValidationException, grails.validation.ValidationException))
        println "-- Exceptions thrown - EntityNotFoundException, catched - EntityNotFoundException"
        runBenchmark(new ExceptionHandlingBenchmark(true, EntityNotFoundException, EntityNotFoundException))
    }

    void runRead(String msg, String bindingMethod = 'grails') {
        println "\n$msg"

        def cacheStatistics = grailsApplication.mainContext.sessionFactory.statistics
        cacheStatistics.clear()

        runBenchmark(new ReadBenchmark(true, true))

        //logMessage "  --not found in the cache and loaded from the db: ${cacheStatistics.getSecondLevelCacheMissCount()}"
        logMessage "  --Second level cache hits: ${cacheStatistics.getSecondLevelCacheHitCount()}"
    }

    void runUpdate(String msg, String bindingMethod = 'grails') {
        println "\n$msg"

        def cacheStatistics = grailsApplication.mainContext.sessionFactory.statistics
        cacheStatistics.clear()

        runBenchmark(new UpdateBenchmark(true))

        logMessage "  --Second level cache hits: ${cacheStatistics.getSecondLevelCacheHitCount()}"
    }

}
