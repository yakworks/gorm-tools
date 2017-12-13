package gpbench

import gorm.tools.async.AsyncBatchSupport
import gorm.tools.dao.DaoApi
import gorm.tools.dao.DaoUtil
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

    RegionDao regionDao
    CountryDao countryDao
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
        println "Gpars pool size (poolSize): " + poolSize
        println "binderType: " + binderType
        println "hibernate.jdbc.batch_size (batchSize): " + batchSize
        println "batchSliceSize: " + batchSliceSize
        println "auditTrailEnabled: " + auditTrailEnabled
        println "refreshableBeansEnabled (eventListenerCount): " + eventListenerCount
        println "Autowire enabled: " + grailsApplication.config.grails.gorm.autowire

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

        // warmUpAndRun("### Gpars - fat props","runFat", binderType)

        warmUpAndRun("### Gpars - Assign Properties, no grails databinding", "runBaselineCompare", binderType)

        if (eventListenerCount)
            warmUpAndRun("### Gpars - with events in refreshable groovy script bean", "runWithEvents", binderType)

        warmUpAndRun("### Dao events", "runDaoEvents", binderType)

        if (auditTrailEnabled)
            warmUpAndRun("### Gpars - audit trail", "runWithAuditTrail", binderType)

        warmUpAndRun("### RXJava, Script executor, etc", "runOther", binderType)

//        warmUpAndRun("### Gpars - standard grails binding with baseline Slower",
//            "runBaselineCompare", 'grails')
//
//        warmUpAndRun("  - Performance problems go away without databinding on traits",
//            "runMultiCoreSlower", 'fast')
//
//        warmUpAndRun("  - Performance problems - standard grails binding with baseline",
//            "runMultiCoreSlower", 'grails')

        runMultiThreadsOther("## Misc sanity checks")

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
        runBenchmark(new GparsBaselineBenchmark(CityBaselineDynamic, bindingMethod))

        runBenchmark(new GparsBaselineBenchmark(City, bindingMethod))
        runBenchmark(new GparsDaoBenchmark(City, bindingMethod))
        //logMessage "\n  - These should all run within about 5% of City and each other"
        //runBenchmark(new GparsBaselineBenchmark(CityAuditTrail, bindingMethod))
    }

    void runWithEvents(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsBaselineBenchmark(CityRefreshableBeanEvents, bindingMethod))
    }

    void runDaoEvents(String msg, String bindingMethod = 'grails') {
        logMessage "\n$msg"
        runBenchmark(new GparsDaoBenchmark(CityDaoMethodEvents, bindingMethod))
        runBenchmark(new GparsDaoBenchmark(CityDaoSpringEvents, bindingMethod))
        runBenchmark(new GparsDaoBenchmark(CityDaoSpringEventsRefreshable, bindingMethod))
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
//		runBenchmark(new GparsDaoBenchmark(City,"setter"))
//		runBenchmark(new GparsDaoBenchmark(City,"fast"))
//
//		runBenchmark(new GparsDaoBenchmark(City,"bindWithSetters"))
//		runBenchmark(new GparsDaoBenchmark(City,"bindFast"))

        new City().attached

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
        insert(countries, countryDao)
        insert(regions, regionDao)

        DaoUtil.flushAndClear()

        assert Country.count() == 275
        assert Region.count() == 3953
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void insert(List<List<Map>> batchList, DaoApi dao) {
        asyncBatchSupport.parallel(batchList) { Map row, args ->
            dao.create(row)
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
