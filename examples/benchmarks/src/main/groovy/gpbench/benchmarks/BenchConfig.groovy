package gpbench.benchmarks

import gorm.tools.async.AsyncBatchSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.GormRepoEntity
import gpbench.helpers.BenchmarkHelper
import gpbench.helpers.JsonReader
import grails.core.GrailsApplication
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.util.StopWatch

@CompileStatic
trait BenchConfig {
    @Autowired
    GrailsApplication grailsApplication
    @Autowired
    JsonReader jsonReader
    @Autowired
    BenchmarkHelper benchmarkHelper
    @Autowired
    EntityMapBinder entityMapBinder
    @Autowired
    AsyncBatchSupport asyncBatchSupport

    @Value('${gpars.poolsize}')
    int poolSize

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
    int loadIterations

    int warmupCycles = 1

    boolean muteConsole = false

    String saveAction
    String msgKey

    List<Map> dataList

    void runBenchMarks() { }

    void loadData() { }

    @CompileDynamic
    void setup() {
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
        benchmarkHelper.initBaseData()

        println "run load json file 3x number of fields"

        //jsonReader._cache = [:]
        //dataList = loadData()
        loadData()
        muteConsole = false
    }

    void warmUpAndRun(String msg, String runMethod, List args = []) {
        warmUp(runMethod, args)
        //warmUp(runMethod, bindingMethod)
        invokeMethod(runMethod, args as Object[])
        //"$runMethod"(msg, bindingMethod)
    }


    @CompileDynamic
    void warmUp(String runMethod, List args = []) {
        muteConsole = true
        def oldLoadIterations = loadIterations
        loadIterations = 1
        System.out.print("Warm up pass with ${loadIterations * 37230} records ")
        (1..warmupCycles).each {
            invokeMethod(runMethod, args as Object[])
        }
        loadIterations = oldLoadIterations
        muteConsole = false
        println ""
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


    @CompileStatic(TypeCheckingMode.SKIP)
    void logMessage(String msg) {
        if (!muteConsole) {
            println msg
        } else {
            System.out.print("*")
        }
    }

    void warmUpAndInsert(Class<?> domainClass) {
        muteConsole = true
        def oldLoadIterations = loadIterations
        loadIterations = 1
        System.out.print("Warm up pass with ${loadIterations * 37230} records ")

        (1..warmupCycles).each {
            insertData(domainClass)
        }
        loadIterations = oldLoadIterations
        muteConsole = false

        //run the real deal
        insertData(domainClass)

        println ""
    }

    @CompileStatic
    void insertData(Class domainClass) {
        StopWatch watch = new StopWatch()
        watch.start()
        if(saveAction == 'batch') {
            insertDataBatch(domainClass)
        } else if(saveAction == 'async') {
            insertDataAsync(domainClass)
        } else {
            insertDataBasic(domainClass)
        }

        watch.stop()
        if (!muteConsole) println "${watch.totalTimeSeconds}s $msgKey - binderType: $binderType, " +
            "${saveAction ? 'saveAction: ' + saveAction : ''} " +
            "- $domainClass.simpleName | ${dataList.size()} rows"
    }


    void insertDataBasic(Class<?> domainClass) {
        for (Map row in dataList) {
            insertRow(domainClass, row)
        }
    }

    void insertDataBatch(Class<?> domainClass) {
        //slice or chunk the list into a list of lists
//        List<List> collatedList = asyncBatchSupport.collate(dataList)
//        City.withTransaction {
//            dataList.eachWithIndex { Map row, int index ->
//                insertRow(domainClass, row)
//                if (index % batchSize == 0) RepoUtil.flushAndClear()
//            }
//        }
    }

    void insertDataAsync(Class<?> domainClass) {
        //slice or chunk the list into a list of lists
        List<List> collatedList = asyncBatchSupport.collate(dataList)
        asyncBatchSupport.parallelBatch(collatedList) { Map row, Map zargs ->
            insertRow(domainClass, row)
        }
    }

    void insertRow(Class<?> domainClass, Map row) {
        GormEntity instance = (GormEntity)domainClass.newInstance()
        //rowClosure.call(instance, row)
        if(binderType == 'grails') {
            ((WebDataBinding)instance).setProperties(row)
        } else if(binderType == 'fast') {
            entityMapBinder.bind(instance, row)
        } else if(binderType == 'setters') {
            instance.invokeMethod('setProps', row)
        }

        if(saveAction == 'validate') {
            instance.validate([failOnError:true])
        } else if(saveAction == 'save') {
            instance.save([failOnError:true])
        } else if(saveAction == 'persist') {
            ((GormRepoEntity)instance).persist()
        }
    }

}
