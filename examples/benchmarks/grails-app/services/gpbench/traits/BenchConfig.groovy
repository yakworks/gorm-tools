package gpbench.traits

import gorm.tools.async.AsyncBatchSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.GormRepoEntity
import gpbench.DataSetupService
import gpbench.benchmarks.AbstractBenchmark
import gpbench.helpers.JsonReader
import grails.core.GrailsApplication
import grails.web.databinding.WebDataBinding
import groovy.json.JsonBuilder
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
    DataSetupService dataSetupService
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

    String createAction
    String benchKey

    List<Map> dataList
    List<Map> warmupDataList

    Map<String,Map> stats

    void runBenchMarks() { }

    void loadData() { }

    void loadWarmUpData() { }

    void cleanup(Class domainClass) { }

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
        dataSetupService.initBaseData()

        loadData()
        loadWarmUpData()

        //blank stats map
        stats = new HashMap<String,Map>()

        muteConsole = false
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void logMessage(String msg) {
        if (!muteConsole) {
            println msg
        } else {
            //System.out.print("*")
        }
    }

    void recordStat(Class domainClass, int dataSize, double timeInSeconds){
        String staticOrDynamic = (domainClass.simpleName.endsWith("Dynamic")) ? "dynamic" : "static"
        String statsKey = "$benchKey-${domainClass.simpleName}"
        stats[statsKey] = [
            benchKey: benchKey,
            binderType: binderType,
            createAction: createAction,
            domainCompile: staticOrDynamic,
            time: timeInSeconds,
            domainClass: domainClass.simpleName,
            dataSize: dataSize,
            batchSize: batchSize,
            poolSize: poolSize
        ]
    }

    void saveStatsToJsonFile(String fileName){
        new File(fileName).write(new JsonBuilder(stats).toPrettyString())
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
}
