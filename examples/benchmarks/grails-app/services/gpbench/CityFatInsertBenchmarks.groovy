package gpbench

import gorm.tools.beans.IsoDateUtil
import gpbench.fat.CityFat
import gpbench.fat.CityFatAuditTrail
import gpbench.fat.CityFatDynamic
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatNoTraitsNoAssoc
import gpbench.fat.CityMethodEvents
import gpbench.fat.CitySpringEvents
import gpbench.fat.CitySpringEventsRefreshable
import gpbench.traits.BenchProcessData
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

/**
 * single threaded sanity checks
 */
@Component
@CompileStatic
class CityFatInsertBenchmarks extends BenchProcessData {

    void run() {
        setup()

        muteConsole = true
        warmup = true
        //"save async" is in twice as the first pass is a warmup run
        ["save async", "create", "validate", "save batch", "save async"].each{ action ->
            createAction = action
            println "-- createAction: $createAction --"
            //the fastest one
            doSettersStatic(CityFatNoTraitsNoAssoc, 'static setters, no associations')
            doSettersStatic(CityFat)
            doSettersDynamic(CityFat)
            doGormToolsRepoPersist(CityFat)
            doGormToolsRepo(CityFat)
            //grailsDataBinderNoTraits(CityFatNoTraits)
            warmup = false
            println "\n" + statsToMarkdownTable()
        }

        println "\n**** The slower ones ****"
        ["create", "validate", "save batch", "save async"].each{
            createAction = it
            println "-- createAction: $createAction --"
            //settersStaticNoAssoc() //this is fast and kept here for reference
            doSettersStatic(CityFatDynamic)
            doSettersDynamic(CityFatDynamic)
            doGormToolsRepo(CityFatDynamic)
            println "\n" + statsToMarkdownTable()
        }

        //logMessage statsToMarkdownTable()

        createAction = "create"
        println "\n-- Grails default DataBinder --"
        processData(CityFatNoTraits, 'grails', 'Grails: default dataBinder, No Traits')

        println "*** Using traits with the Grails default DataBinder is super slow, see bug report"
        processData(CityFat, 'grails', 'Grails: default dataBinder w/Traits')

        println "\n**Stats to insert ${dataList.size()} items on City Domains with 32+ fields**"
        println statsToMarkdownTable()

    }

    @CompileDynamic
    void runEvents() {
        setup()
        muteConsole = true

        //do a warmup pass
        warmup = true
        [/*"save batch", */"save async"].each{ action ->
            createAction = action
            doGormToolsRepo(CityFat)
        }
        warmup = false

        ["save batch", "save async"].each{ action ->
            createAction = action
            doGormToolsRepo(CityFat)
            doGormToolsRepo(CitySpringEvents, 'Repository Spring Events')
            doGormToolsRepo(CitySpringEventsRefreshable, 'Repository Refreshable Bean Spring Events')
            doGormToolsRepo(CityMethodEvents, 'Repository Method Events')
            scriptEngine(CityFat)
            doGormToolsRepo(CityFatAuditTrail, 'audit-trail: @AuditStamp')
            muteConsole = false
        }

        println "\n**Stats to insert ${dataList.size()} items on City Domains with 32+ fields**"
        println statsToMarkdownTable(['Benchmark'] , ['save batch', 'save async'] )

    }

    @CompileDynamic
    void runIssues() {
        setup()
        //muteConsole = true

        // this validates ultra slow when association is set with get.
        // create runs fine so its not the actual get.
        ['create', 'validate'].each{ action ->
            createAction = action
            processData(CityFat, 'settersDynamic-useGet', 'set associations using get vs load')
        }
        println statsToMarkdownTable(['Benchmark'] , ['create', 'validate'] )

    }

    @CompileDynamic
    void scriptEngine(Class domainClass, String benchKey = 'Repository runs script each row'){
        binderType = 'gorm-tools'
        this.benchKey = benchKey

        GroovyScriptEngine scriptEngine = new GroovyScriptEngine("src/main/resources", grailsApplication.classLoader)
        def scriptinsert = scriptEngine.run("insert-city.groovy", new Binding([dataBinder: binderType]))

        List<Map> data = data()

        runAndRecord(domainClass, data){
            if(createAction == 'save batch') {
                saveBatch(domainClass, data){ Class dc, Map row ->
                    scriptinsert.insertRow(domainClass, row)
                }
            } else if(createAction == 'save async') {
                asyncBatchSupport.parallelCollate(data) { row, zargs ->
                    scriptinsert.insertRow(domainClass, row)
                }
            }
        }
        cleanup(domainClass, data.size())
    }

    @Override
    void loadData(){
        dataList = jsonReader.loadCityFatData(multiplyData)
    }

    @Override
    void loadWarmUpData(){
        warmupDataList = jsonReader.loadCityFatData(1)
    }

    @CompileDynamic
    @Override
    void setterDynamic(instance, row, useGet = false) {
        instance.with {
            name = row['name']
            shortCode = row['shortCode']
            state = row['state']
            countryName = row['countryName']
            latitude = row['latitude'] as BigDecimal
            longitude = row['longitude'] as BigDecimal

            name2 = row['name2']
            shortCode2 = row['shortCode2']
            state2 = row['state2']
            countryName2 = row['countryName2']
            latitude2 = row['latitude2'] as BigDecimal
            longitude2 = row['longitude2'] as BigDecimal

            name3 = row['name3']
            shortCode3 = row['shortCode3']
            state3 = row['state3']
            countryName3 = row['countryName3']
            latitude3 = row['latitude3'] as BigDecimal
            longitude3 = row['longitude3'] as BigDecimal
            //this.properties = row
            date1 = IsoDateUtil.parse(row['date1'] as String)
            date2 = IsoDateUtil.parseLocalDate(row['date2'] as String) //DateUtil.parse(row['date2'] as String)
            date3 = IsoDateUtil.parseLocalDateTime(row['date3'] as String)
            date4 = IsoDateUtil.parseLocalDate(row['date4'] as String)
        }
        setAssociation(instance, "region", Region, row, useGet)
        setAssociation(instance, "region2", Region, row, useGet)
        setAssociation(instance, "region3", Region, row, useGet)
        setAssociation(instance, "country", Country, row, useGet)
        setAssociation(instance, "country2", Country, row, useGet)
        setAssociation(instance, "country3", Country, row, useGet)
    }

    @CompileDynamic
    void setAssociation(instance, String key, Class assocClass, Map row, boolean useGet = false) {
        if (row[key]) {
            Long id = row[key]['id'] as Long
            instance[key] = useGet ? assocClass.get(id) : assocClass.load(id)
        }
    }

}
