package gpbench.traits

import gorm.tools.async.AsyncBatchSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.api.RepositoryApi
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

import java.text.DecimalFormat

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

    @Value('${benchmark.multiplyData}')
    int multiplyData

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

    RepositoryApi repository

    @CompileDynamic
    void setRepo(Class domainClass){
        repository = domainClass.repo
    }

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

    void recordStat(Class domainClass, int dataSize, double time){
        String staticOrDynamic = (domainClass.simpleName.endsWith("Dynamic")) ? "dynamic" : "static"
        String statsKey = "$benchKey-${domainClass.simpleName}"
        if(!stats[statsKey]) {
            stats[statsKey] = [
                "Benchmark"         : benchKey,
                binderType       : binderType,
                (createAction): time,
                "Domain @Compile"    : staticOrDynamic,
                //time             : time,
                domainClass      : domainClass.simpleName,
                dataSize         : dataSize,
                batchSize        : batchSize,
                poolSize         : poolSize
            ]
        } else{
            stats[statsKey][createAction] = time
        }
    }

    void saveStatsToJsonFile(String fileName){
        new File(fileName).write(new JsonBuilder(stats).toPrettyString())
    }

    @CompileDynamic
    String statsToMarkdownTable(){
        Map mapLen = [:]
        List cols = ['Benchmark', "Domain @Compile", 'create', 'validate', 'save batch', 'save async']

        //sort by create
        List statList = stats.collect{k,v ->
            v.findAll{true} //copy the map
        }.sort { it['create'] }
        List timeFields = ['create', 'validate', 'save batch', 'save async']
        //format the time fields
        statList = statList.collect { stat ->
            timeFields.each { tf ->
                if(stat.containsKey(tf)) stat[tf] = new DecimalFormat("##0.00s").format(stat[tf])
            }
            stat
        }

        //get max length for each val
        statList[0].keySet().each{ statKey ->
            mapLen[statKey] = statList.collect{it[statKey].toString()}*.length().max()
        }
        //see if titles are longer
        cols.each {
            mapLen[it] = mapLen[it] ?: it.length()
            if(it.length() > mapLen[it]) mapLen[it] = it.length()
        }

        //build markdown table
        String table = "| "

        cols.each{
            def val = timeFields.contains(it) ? it.padLeft(mapLen[it]) : it.padRight(mapLen[it])
            table = table + "$val | "
        }
        table += "\n|"
        cols.each{
            table = table + "-${"".padRight(mapLen[it],'-')}-|"
        }
        //table += "\n| "
        statList.each {Map vmap ->
            table += "\n| "
            cols.each { String col->
                def val = vmap[col]?:""
                val = timeFields.contains(col) ? val.padLeft(mapLen[col]) : val.padRight(mapLen[col])
                table = table + "$val | "
            }
        }
        return table
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
        def oldLoadIterations = multiplyData
        multiplyData = 1
        System.out.print("Warm up pass with ${multiplyData * 37230} records ")
        (1..warmupCycles).each {
            invokeMethod(runMethod, args as Object[])
        }
        multiplyData = oldLoadIterations
        muteConsole = false
        println ""
    }
}
