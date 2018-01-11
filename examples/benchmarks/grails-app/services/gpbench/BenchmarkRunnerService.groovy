package gpbench

import gorm.tools.async.AsyncBatchSupport
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gpbench.benchmarks.*
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.helpers.BenchmarkHelper
import gpbench.helpers.CsvReader
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.dao.DataAccessException

class BenchmarkRunnerService {

    AsyncBatchSupport asyncBatchSupport
    static transactional = false

    @Value('${gpars.poolsize}')
    int poolSize

    //this should match the hibernate.jdbc.batch_size in datasources
    @Value('${hibernate.jdbc.batch_size}')
    int batchSize

    @Value('${benchmark.batchSliceSize}')
    int batchSliceSize

    @Value('${benchmark.eventListenerCount}')
    int eventListenerCount

    @Value('${grails.plugin.audittrail.enabled}')
    boolean auditTrailEnabled

    @Value('${benchmark.binder.type}')
    String binderType

    @Value('${benchmark.loadIterations}')
    int loadIterations = System.getProperty("load.iterations", "3").toInteger()
    int warmupCycles = 1
    boolean muteConsole = false

    RegionRepo regionRepo
    CountryRepo countryRepo
    CityRepo cityRepo
    GrailsApplication grailsApplication

    CsvReader csvReader
    BenchmarkHelper benchmarkHelper

    //@CompileStatic
    void runBenchMarks() {
        println "--- Environment info ---"
        //println "Max memory: " + (Runtime.getRuntime().maxMemory() / 1024 )+ " KB"
        //println "Total Memory: " + (Runtime.getRuntime().totalMemory() / 1024 )+ " KB"
        //println "Free memory: " + (Runtime.getRuntime().freeMemory() / 1024 ) + " KB"
        println "Available processors: " + Runtime.getRuntime().availableProcessors()
        println "Gpars pool size (gpars.poolsize): " + poolSize
        println "binderType: " + binderType
        println "hibernate.jdbc.batch_size (jdbcBatchSize): " + batchSize
        println "batchSliceSize: " + batchSliceSize
        println "auditTrailEnabled: " + auditTrailEnabled
        println "refreshableBeansEnabled (eventListenerCount): " + eventListenerCount
        println "Autowire enabled (autowire.enabled): " + grailsApplication.config.grails.gorm.autowire

        //load base country and city data which is used by all benchmarks
        benchmarkHelper.truncateTables()
        prepareBaseData()

        muteConsole = false

        //real benchmarks starts here
        println "\n- Running Benchmarks, loading ${loadIterations * 37230} records each run"

        if (System.getProperty("runSingleThreaded", "false").toBoolean()) {
            println "-- single threaded - no gpars"
            runBenchmark(new SimpleBatchInsertBenchmark(true))
        }

        //warmUpAndRun("### Exception handling", "runWithExceptions", binderType)

        // warmUpAndRun("### Gpars - fat props","runFat", binderType)

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

    void warmUp(String runMethod, String bindingMethod) {
        muteConsole = true
        def oldLoadIterations = loadIterations
        loadIterations = 1
        System.out.print("Warm up pass with ${loadIterations * 37230} records ")
        //runMultiCoreGrailsBaseline("")
        (1..warmupCycles).each {
            "$runMethod"("", bindingMethod)
        }
        loadIterations = oldLoadIterations
        muteConsole = false
        println ""
    }

    void warmUpAndRun(String msg, String runMethod, String bindingMethod = 'grails') {
        warmUp(runMethod, bindingMethod)
        //warmUp(runMethod, bindingMethod)
        "$runMethod"(msg, bindingMethod)
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

    @CompileStatic(TypeCheckingMode.SKIP)
    void logMessage(String msg) {
        if (!muteConsole) {
            println msg
        } else {
            System.out.print("*")
        }
    }

    void prepareBaseData() {
        benchmarkHelper.executeSqlScript("test-tables.sql")
        List<List<Map>> countries = csvReader.read("Country").collate(batchSize)
        List<List<Map>> regions = csvReader.read("Region").collate(batchSize)
        insert(countries, countryRepo)
        insert(regions, regionRepo)

        assert Country.count() == 275
        assert Region.count() == 3953
    }

    void insert(List<List<Map>> batchList, RepositoryApi repo) {
        asyncBatchSupport.parallel(batchList) { List<Map> list, Map args ->
            repo.batchCreate(list)
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void runBenchmark(AbstractBenchmark benchmark, boolean mute = false) {
        if (benchmark.hasProperty("poolSize")) benchmark.poolSize = poolSize
        if (benchmark.hasProperty("batchSize")) benchmark.batchSize = batchSize
        if (benchmark.hasProperty("repeatedCityTimes")) benchmark.repeatedCityTimes = loadIterations
        if (benchmark.hasProperty("disableSave")) benchmark.disableSave = disableSave

        autowire(benchmark)
        benchmark.run()
        logMessage "${benchmark.timeTaken}s for $benchmark.description"
        //if(!MUTE_CONSOLE) println "${benchmark.timeTaken}s for $benchmark.description"
    }


    @CompileStatic
    void autowire(def bean) {
        grailsApplication.mainContext.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
    }

}
