package gpbench.traits

import java.text.DecimalFormat

import groovy.json.JsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.StopWatch

import gorm.tools.async.AsyncSupport
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gpbench.helpers.CsvReader
import gpbench.helpers.JsonReader
import grails.core.GrailsApplication

@CompileStatic
trait BenchConfig {
    @Autowired
    GrailsApplication grailsApplication
    @Autowired
    HibernateDatastore hibernateDatastore
    @Autowired
    JsonReader jsonReader
    @Autowired
    EntityMapBinder entityMapBinder
    @Autowired
    AsyncSupport asyncSupport
    @Autowired
    CsvReader csvReader

    @Value('${gpars.poolsize}')
    int poolSize

    @Value('${hibernate.jdbc.batch_size}')
    int batchSize

    @Value('${benchmark.batchSliceSize}')
    int batchSliceSize

    @Value('${benchmark.eventListenerCount}')
    int eventListenerCount

    @Value('${gorm.tools.audit.enabled:true}')
    boolean auditTrailEnabled

    @Value('${benchmark.binder.type}')
    String binderType

    @Value('${benchmark.multiplyData}')
    int multiplyData

    int warmupCycles = 1

    boolean muteConsole

    boolean warmup

    String createAction
    String benchKey

    List<Map> dataList
    List<Map> warmupDataList

    Map<String,Map> stats


    GormRepo repository

    void loadData() { }

    void loadWarmUpData() { }

    List<Map> data(){
        getWarmup() ? warmupDataList : dataList
    }


    void setRepo(Class domainClass){
        repository = (GormRepo) RepoUtil.findRepo(domainClass) //domainClass.repo
    }

    void setup() {

        loadWarmUpData()

        loadData()


        //blank stats map
        stats = [:]

        muteConsole = false
    }

    void runAndRecord(Class domainClass, List<Map> data, Closure closure) {
        //logMessage "processData with ${dataList.size()} records. ${domainClass.simpleName}-$saveAction - binderType: $binderType"

        StopWatch watch = new StopWatch()
        watch.start()

        closure.call()

        watch.stop()

        Integer dataSize = data.size()

        String time = new DecimalFormat("##0.00s").format(watch.totalTimeSeconds)
        logMessage "$time $benchKey - binderType: $binderType, " +
            "${createAction ? 'createAction: ' + createAction : ''} " +
            "- $domainClass.simpleName | $dataSize rows"

        recordStat(domainClass, dataSize, watch.totalTimeSeconds)

    }

    @CompileDynamic
    void printEnvironment(){
        println "\n--- Environment info ---"
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
        println "- Gorm -----------------------------------"
        println "  Autowire enabled (autowire.enabled): " + grailsApplication.config.grails.gorm.autowire
        println "  flushMode: " + hibernateDatastore.defaultFlushModeName
        println "  Second Level Cache: " + grailsApplication.config.hibernate.cache.use_second_level_cache
        println "-----------------------------------------"

    }

    void logMessage(String msg) {
        if (!muteConsole) {
            println msg
        } else {
            System.out.print("*")
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
    String statsToMarkdownTable(List columns = ['Benchmark', "Domain @Compile"] ,
                                List measures = ['create', 'validate', 'save batch', 'save async'] ){
        Map mapLen = [:]
        List cols = columns + measures //['Benchmark', "Domain @Compile", 'create', 'validate', 'save batch', 'save async']

        //sort by create
        List timeFields = measures //['create', 'validate', 'save batch', 'save async']

        List statList = stats.collect{k,v ->
            v.findAll{true} //copy the map
        }.sort { it['create'] }

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
            table += "$val | "
        }
        table += "\n|"
        cols.each{
            table += "-${"".padRight(mapLen[it],'-')}"
            table += (timeFields.contains(it) ? ":|" : "-|")
        }
        //table += "\n| "
        statList.each {Map vmap ->
            table += "\n| "
            cols.each { String col->
                def val = vmap[col]?:""
                val = timeFields.contains(col) ? val.padLeft(mapLen[col]) : val.padRight(mapLen[col])
                table += "$val | "
            }
        }
        return table
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
