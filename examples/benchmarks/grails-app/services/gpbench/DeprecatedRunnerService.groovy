package gpbench


import gorm.tools.async.AsyncSupport
import gorm.tools.repository.model.RepositoryApi
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gpbench.basic.*
import gpbench.benchmarks.AbstractBenchmark
import gpbench.benchmarks.GparsBaselineBenchmark
import gpbench.benchmarks.GparsRepoBenchmark
import gpbench.benchmarks.concept.BatchInsertWithDataFlowQueueBenchmark
import gpbench.benchmarks.concept.ExceptionHandlingBenchmark
import gpbench.benchmarks.concept.RxJavaBenchmark
import gpbench.benchmarks.read.ReadBenchmark
import gpbench.benchmarks.update.GparsBaseLineUpdateBenchmark
import gpbench.benchmarks.update.RepoUpdateBenchmark
import gpbench.benchmarks.update.UpdateBenchmark
import gpbench.helpers.CsvReader
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.dao.DataAccessException

class DeprecatedRunnerService {

    AsyncSupport asyncSupport
    DataSetup dataSetup

    @Value('${gpars.poolsize}')
    int poolSize

    //this should match the hibernate.jdbc.batch_size in datasources
    @Value('${hibernate.jdbc.batch_size}')
    int batchSize

    @Value('${benchmark.batchSliceSize}')
    int batchSliceSize

    @Value('${benchmark.eventListenerCount}')
    int eventListenerCount

    @Value('${gorm.tools.audit.enabled}')
    boolean auditTrailEnabled

    @Value('${benchmark.binder.type}')
    String binderType

    @Value('${benchmark.multiplyData}')
    int multiplyData //= System.getProperty("multiplyData", "3").toInteger()

    int warmupCycles = 1
    boolean muteConsole = false

    RegionRepo regionRepo
    CountryRepo countryRepo
    CityBasicRepo cityRepo
    GrailsApplication grailsApplication

    CsvReader csvReader

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
        dataSetup.truncateTables()
        prepareBaseData()

        muteConsole = false

        //real benchmarks starts here
        println "\n- Running Benchmarks, loading ${multiplyData * 37230} records each run"

        if (System.getProperty("runSingleThreaded", "false").toBoolean()) {
            println "-- single threaded - no gpars"
            //runBenchmark(new SimpleBatchInsertBenchmark(true))
        }

        //warmUpAndRun("### Exception handling", "runWithExceptions", binderType)

        // warmUpAndRun("### Gpars - fat props","runFat", binderType)

        //warmUpAndRun("### Gpars - update benchmarks", "runUpdateBenchmarks", binderType)
        warmUpAndRun("### Gpars - baseline", "runBaselineCompare", binderType)

        //warmUpAndRun("### Repo events - set audit fields", "runRepoEvents", binderType)

        if (auditTrailEnabled)
            warmUpAndRun("### Gpars - audit trail", "runWithAuditTrail", binderType)

        if (eventListenerCount)
            warmUpAndRun("### Gpars - with events in refreshable groovy script bean", "runWithEvents", binderType)

        // warmUpAndRun("### RXJava, Script executor, etc", "runOther", binderType)

        //warmUpAndRun("  - Performance problems go away without databinding on traits", "runMultiCoreSlower", 'fast')

        warmUpAndRun("### Reading record", "runRead", binderType)

        warmUpAndRun("### Updating records", "runUpdate", binderType)

        //runMultiThreadsOther("## Misc sanity checks")

        System.exit(0)
    }

    void warmUp(String runMethod, String bindingMethod) {
        muteConsole = true
        def oldLoadIterations = multiplyData
        multiplyData = 1
        System.out.print("Warm up pass with ${multiplyData * 37230} records ")
        //runMultiCoreGrailsBaseline("")
        (1..warmupCycles).each {
            "$runMethod"("", bindingMethod)
        }
        multiplyData = oldLoadIterations
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
        runBenchmark(new GparsBaselineBenchmark(CityBasic, bindingMethod))
        runBenchmark(new GparsRepoBenchmark(CityBasic, bindingMethod))
    }

    void runUpdateBenchmarks(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaseLineUpdateBenchmark(CityBaseline))
        runBenchmark(new GparsBaseLineUpdateBenchmark(CityBasic))
        runBenchmark(new RepoUpdateBenchmark(CityBaseline))
        runBenchmark(new RepoUpdateBenchmark(CityBasic))
    }

    void runWithEvents(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaselineBenchmark(CityRefreshableBeanEvents, bindingMethod))
    }

    void runOther(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new RxJavaBenchmark(CityBasic, bindingMethod))
    }

    void runMultiThreadsOther(String msg) {
        println "\n$msg"
        runBenchmark(new BatchInsertWithDataFlowQueueBenchmark('gorm-tools'))

        logMessage "  - using copy instead of binding and no validation, <10% faster"
        runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic, 'gorm-tools', false))

        new CityBasic().attached

    }

    void runWithExceptions(String msg, String binding) {
        logMessage "\n$msg"
        println "-- single threaded without exception, just for comparison"
        //runBenchmark(new SimpleBatchInsertBenchmark(true))
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
        dataSetup.executeSqlScript("test-tables.sql")
        List<List<Map>> countries = csvReader.read("Country").collate(batchSize)
        List<List<Map>> regions = csvReader.read("Region").collate(batchSize)
        insert(countries, countryRepo)
        insert(regions, regionRepo)

        assert Country.count() == 275
        assert Region.count() == 3953
    }

    void insert(List<List<Map>> batchList, RepositoryApi repo) {
        asyncSupport.parallel(batchList) { List<Map> list, Map args ->
            repo.batchCreate(list)
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void runBenchmark(AbstractBenchmark benchmark, boolean mute = false) {
        if (benchmark.hasProperty("poolSize")) benchmark.poolSize = poolSize
        if (benchmark.hasProperty("batchSize")) benchmark.batchSize = batchSize
        if (benchmark.hasProperty("repeatedCityTimes")) benchmark.repeatedCityTimes = multiplyData
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
