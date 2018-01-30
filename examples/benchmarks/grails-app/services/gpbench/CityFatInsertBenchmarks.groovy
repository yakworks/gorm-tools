package gpbench

import gorm.tools.beans.IsoDateUtil
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatNoTraitsNoAssoc
import gpbench.fat.CityMethodEvents
import gpbench.fat.CitySpringEvents
import gpbench.fat.CitySpringEventsRefreshable
import gpbench.traits.BenchDataInsert
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

/**
 * single threaded sanity checks
 */
@Component
@CompileStatic
class CityFatInsertBenchmarks extends BenchDataInsert {

    void run() {
        setup()

        muteConsole = true
        //"save async" is in twice as the first pass is a warmup run
        ["save async", "create", "validate", "save batch", "save async"].each{ action ->
            createAction = action
            println "-- createAction: $createAction --"
            settersStaticNoAssoc()
            settersStatic(CityFat)
            settersDynamic(CityFat)
            gormToolsRepo(CityFat)
            //grailsDataBinderNoTraits(CityFatNoTraits)
            println "\n" + statsToMarkdownTable()
        }

        println "\n**** The slower ones ****"
        ["create", "validate", "save batch", "save async"].each{
            createAction = it
            println "-- createAction: $createAction --"
            //settersStaticNoAssoc() //this is fast and kept here for reference
            settersStatic(CityFatDynamic)
            settersDynamic(CityFatDynamic)
            gormToolsRepo(CityFatDynamic)
            println "\n" + statsToMarkdownTable()
        }

        //logMessage statsToMarkdownTable()

        createAction = "create"
        println "\n-- Grails default DataBinder --"
        grailsDataBinderNoTraits(CityFatNoTraits)
        //grailsDataBinderNoTraits(CityFatNoTraitsDynamic)

        println "*** Using traits with the Grails default DataBinder is super slow, see bug report"
        grailsDataBinderWithTraits(CityFat)
        //warmUpAndInsert(CityFat)

        println "\n**Stats to insert ${dataList.size()} items on City Domains with 32+ fields**"
        println statsToMarkdownTable()

    }

    @CompileDynamic
    void runEvents() {
        setup()
        muteConsole = true
        //doo a warmup pass
        warmup = true
        createAction = "save batch"
        ["save batch", "save async"].each{ action ->
            createAction = action
            gormToolsRepo(CityFat)
        }
        warmup = false

        ["save batch", "save async"].each{ action ->
            createAction = action
            gormToolsRepo(CityFat)
            gormToolsRepo(CitySpringEvents, 'Repository Spring Events')
            gormToolsRepo(CitySpringEventsRefreshable, 'Repository Refreshable Bean Spring Events')
            gormToolsRepo(CityMethodEvents, 'Repository Method Events')
            scriptEngine(CityFat)
            muteConsole = false
        }

        println "\n**Stats to insert ${dataList.size()} items on City Domains with 32+ fields**"
        println statsToMarkdownTable(['Benchmark'] , ['save batch', 'save async'] )

    }

    //@Transactional
    void settersStaticNoAssoc(){
        binderType = 'settersStatic'
        benchKey = 'setters static, no assocs'
        //no warm up on this one
        insertData(CityFatNoTraitsNoAssoc, data())
    }

    void settersStatic(Class domainClass){
        binderType = 'settersStatic'
        benchKey = 'setters static'
        insertData(domainClass, data())
        //insertData(CityFatDynamic, dataList)
    }

    void settersDynamic(Class domainClass){
        binderType = 'settersDynamic'
        benchKey = 'setters dynamic'
        insertData(domainClass, data())
    }

    void gormToolsRepo(Class domainClass, String benchKey = 'gorm-tools: repository & fast binder'){
        binderType = 'gorm-tools'
        this.benchKey = benchKey
        insertData(domainClass, data())
    }

    void grailsDataBinderNoTraits(Class domainClass){
        binderType = 'grails'
        benchKey = 'Grails default DataBinder, No Traits'
        insertData(domainClass, data())
    }

    void grailsDataBinderWithTraits(Class domainClass){
        benchKey = 'Grails DataBinder w/Traits'
        binderType = 'grails'
        insertData(domainClass, data())
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
                insertDataBatch(domainClass, data){ Class dc, Map row ->
                    scriptinsert.insertRow(domainClass, row)
                }
            } else if(createAction == 'save async') {
                asyncBatchSupport.parallelCollate(data) { row, zargs ->
                    scriptinsert.insertRow(domainClass, row)
                }
            }
        }
        assertAndCleanup(domainClass, data.size())
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
    void setterDynamic(instance, row) {
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
        setAssociation(instance, "region", Region, row)
        setAssociation(instance, "region2", Region, row)
        setAssociation(instance, "region3", Region, row)
        setAssociation(instance, "country", Country, row)
        setAssociation(instance, "country2", Country, row)
        setAssociation(instance, "country3", Country, row)
    }

    @CompileDynamic
    void setAssociation(instance, String key, Class assocClass, Map row) {
        if (row[key]) {
            Long id = row[key]['id'] as Long
            instance[key] = assocClass.load(id)
        }
    }

}
